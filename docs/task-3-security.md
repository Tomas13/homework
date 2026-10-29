# Task 3: Authentication and authorization with OAuth 2 and JWT

## Goal
- Use **OAuth 2** for user authentication.
- Issue **JWT** access tokens to authenticated users and validate them on every API request.
- Restrict endpoints by role: **USER** and **ADMIN**.

## Architecture

The application plays two OAuth 2 roles at once:

1. **Authorization server** (Spring Authorization Server, part of Spring Security 7). It signs users in with a login
   form, runs the *authorization code flow with PKCE*, and issues JWTs signed with an RSA key.
2. **Resource server.** `/api/**` accepts only requests with a valid `Authorization: Bearer <JWT>` header. The
   signature, expiry and issuer are checked, and roles are read from the token's `roles` claim.

```
Browser / curl ──► /oauth2/authorize ──► /login (form) ──► redirect with ?code=...
              ──► POST /oauth2/token (code + PKCE verifier) ──► JWT access token
              ──► /api/products with "Authorization: Bearer <JWT>" ──► role check ──► controller
```

## Dependencies

```groovy
implementation 'org.springframework.boot:spring-boot-starter-security-oauth2-authorization-server'
implementation 'org.springframework.boot:spring-boot-starter-security-oauth2-resource-server'
testImplementation 'org.springframework.boot:spring-boot-starter-webmvc-test'
testImplementation 'org.springframework.boot:spring-boot-starter-security-test'
```

## Configuration: `security/SecurityConfig.java`

### Security filter chains (checked in order)

| Order | Matches | Rules |
|---|---|---|
| 1 | Authorization server endpoints (`/oauth2/*`, `/.well-known/*`) | Authenticated; browsers are redirected to `/login` |
| 2 | Actuator (`/actuator/**`), added in [task 6](task-6-actuator.md) | `health`, `info`, `prometheus` public; the rest ADMIN only |
| 3 | `/api/**` | JWT required. `GET`: USER or ADMIN. `POST`/`PUT`/`DELETE`: ADMIN only. Stateless, no CSRF (bearer tokens aren't sent automatically by browsers) |
| 4 | Everything else | `/`, `/index.html`, `/authorized`, `/error` public; form login at `/login`; logout returns to `/` |

### Users (in memory, passwords hashed with the delegating `PasswordEncoder`)

| User | Password | Roles |
|---|---|---|
| `user` | `password` | USER |
| `admin` | `admin` | USER, ADMIN |

### OAuth 2 clients

| Client id | Type | Authentication | Redirect URI | Used by |
|---|---|---|---|---|
| `product-client` | confidential | `client_secret_basic` (`secret`) + PKCE | `http://127.0.0.1:8080/authorized` | curl / `scripts/get-token.sh` / Postman |
| `product-ui` | public | none, PKCE only (browser code can't keep a secret) | `http://127.0.0.1:8080/` | browser UI (`static/index.html`) |

Both use scope `products`, no consent screen, and 30-minute access tokens.

### JWT
- Signed with a 2048-bit **RSA key generated at startup** (`jwkSource`). Tokens become invalid after a restart.
- The public key is published at `/oauth2/jwks`.
- `rolesTokenCustomizer` adds a `roles` claim to access tokens:
  ```json
  { "sub": "admin", "aud": "product-client", "scope": ["products"], "roles": ["ADMIN", "USER"], "exp": ... }
  ```
- `jwtAuthenticationConverter` maps `roles` back to `ROLE_ADMIN` / `ROLE_USER` authorities for `hasRole(...)` checks.

### Notes
- **PKCE is required** for both clients. Spring Security 7 enforces it by default, following OAuth 2.1.
- Use **`127.0.0.1`, not `localhost`**: the authorization server rejects `localhost` redirect URIs. The browser UI
  switches to `127.0.0.1` automatically.

## Using it

### In the browser (recommended)
1. Open **http://127.0.0.1:8080/**.
2. Click **Log in** and sign in as `user` or `admin`.
3. The page shows your name, roles and token lifetime, plus the product table with Add/Edit/Delete.
   - `user`: the list loads, and changes show *HTTP 403 Forbidden*.
   - `admin`: everything works.
4. **Log out** ends the session; you can then sign in as the other user.

### From the command line
`scripts/get-token.sh` runs the whole authorization code + PKCE flow with curl and prints the access token:

```bash
TOKEN=$(scripts/get-token.sh admin admin)
curl -H "Authorization: Bearer $TOKEN" http://127.0.0.1:8080/api/products
```

Set `PORT=8081` when the server runs on another port.

### Postman
Authorization tab → OAuth 2.0 → grant type *Authorization Code (With PKCE)*:

| Setting | Value |
|---|---|
| Auth URL | `http://127.0.0.1:8080/oauth2/authorize` |
| Access Token URL | `http://127.0.0.1:8080/oauth2/token` |
| Client ID / Secret | `product-client` / `secret` |
| Callback URL | `http://127.0.0.1:8080/authorized` |
| Scope | `products` |

## Verified behaviour

| Request | Result |
|---|---|
| No token | `401` |
| Invalid token | `401` |
| `user`: GET | `200` |
| `user`: POST / PUT / DELETE | `403` |
| `admin`: POST | `201` |
| `admin`: PUT | `200` |
| `admin`: DELETE | `204` |
| Wrong password | no token issued |

## Automated tests
- `ProductControllerSecurityTest`: no token → 401; USER can read, but gets 403 on create and delete; ADMIN can create.
- `BrowserUiTest`: the UI page is public, and the `product-ui` client is accepted and redirected to `/login`.

## Limitations (demo setup)
- Users, client secrets and the signing key are in memory and hard-coded or generated at startup.
- For production: store users in a database, load the RSA key from a keystore or secret manager, and use HTTPS.
