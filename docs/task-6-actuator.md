# Task 6: Spring Boot Actuator, custom health indicators and Prometheus metrics

## Goal
- Enable **Spring Boot Actuator** for monitoring and management.
- Implement **custom health indicators** for application-specific health.
- Add **custom metrics for Prometheus** to observe application behaviour.

## Dependencies

```groovy
implementation 'org.springframework.boot:spring-boot-starter-actuator'
runtimeOnly    'io.micrometer:micrometer-registry-prometheus'
```

## Configuration (`application.properties`)

```properties
management.endpoints.web.exposure.include=health,info,metrics,prometheus,loggers,env,beans,mappings
management.endpoint.health.show-details=when-authorized
management.endpoint.health.show-components=always
management.endpoint.health.roles=ADMIN
management.endpoint.env.show-values=when-authorized
management.endpoint.env.roles=ADMIN
management.info.env.enabled=true
management.info.java.enabled=true
management.metrics.tags.application=${spring.application.name}   # adds application="homework" to every metric

info.app.name=${spring.application.name}
info.app.description=Product catalog REST API secured with OAuth 2 and JWT
```

## Endpoints and access

Secured by `actuatorSecurityFilterChain` in `SecurityConfig` (JWT, same as the API):

| Endpoint | Access | Purpose |
|---|---|---|
| `/actuator/health` | public (details only for ADMIN) | liveness/readiness for load balancers |
| `/actuator/info` | public | app name, description, Java version |
| `/actuator/prometheus` | public | scraped by Prometheus |
| `/actuator/metrics`, `/loggers`, `/env`, `/beans`, `/mappings` | ADMIN | management and debugging |

Verified: `/actuator/metrics` gives no token → `401`, `user` → `403`, `admin` → `200`.

## Custom health indicators (`monitoring/`)

Both extend `AbstractHealthIndicator` (Boot 4 package `org.springframework.boot.health.contributor`). Any exception
in a check automatically reports `DOWN`.

### `productCatalog`: `ProductCatalogHealthIndicator`
Queries the catalog through `ProductRepository`. It is `DOWN` if the database can't be queried.

```json
"productCatalog": { "status": "UP",
  "details": { "products": 3, "outOfStock": 0, "lowStock": 1, "lowStockThreshold": 5 } }
```

### `tokenSigningKey`: `TokenSigningKeyHealthIndicator`
Checks that an RSA private key for signing JWTs is available. Without it nobody can log in, so it reports `DOWN`.

```json
"tokenSigningKey": { "status": "UP", "details": { "keyId": "ad5ce065-...", "keySize": 2048 } }
```

## Custom Prometheus metrics (`monitoring/`)

| Prometheus name | Type | Source | Meaning |
|---|---|---|---|
| `products_operations_total{operation="create\|update\|delete"}` | counter | `ProductMetrics.recordOperation`, called from `ProductController` | changes made through the API |
| `products_catalog_size` | gauge | `ProductMetrics` (`repository.count()`) | products in the catalog |
| `products_out_of_stock` | gauge | `ProductMetrics` (`countByQuantityLessThanEqual(0)`) | products with no stock |
| `auth_logins_total{result="success\|failure"}` | counter | `LoginMetrics`, listening to Spring Security authentication events | login-page sign-ins (the JWT check on API calls is not counted) |

Micrometer names like `products.operations` become `products_operations_total` in Prometheus format.
`countByQuantityLessThanEqual` was added to `ProductRepository` for the stock numbers.

Sample `/actuator/prometheus` output after a few logins and changes:
```
auth_logins_total{application="homework",result="failure"} 1.0
auth_logins_total{application="homework",result="success"} 2.0
products_catalog_size{application="homework"} 3.0
products_operations_total{application="homework",operation="create"} 1.0
products_operations_total{application="homework",operation="delete"} 1.0
products_operations_total{application="homework",operation="update"} 1.0
products_out_of_stock{application="homework"} 1.0
```

## Trying it

```bash
curl http://127.0.0.1:8080/actuator/health
curl http://127.0.0.1:8080/actuator/info
curl -s http://127.0.0.1:8080/actuator/prometheus | grep -E "^(products_|auth_logins)"

TOKEN=$(scripts/get-token.sh admin admin)
curl -H "Authorization: Bearer $TOKEN" http://127.0.0.1:8080/actuator/health        # with details
curl -H "Authorization: Bearer $TOKEN" "http://127.0.0.1:8080/actuator/metrics/products.operations?tag=operation:create"
```

### Prometheus scrape config

```yaml
scrape_configs:
  - job_name: homework
    metrics_path: /actuator/prometheus
    static_configs:
      - targets: ['127.0.0.1:8080']
```

## Automated tests
`monitoring/ActuatorTest`:
- health is public but hides details
- ADMIN sees the details of both custom indicators
- `/actuator/prometheus` contains all custom metrics after an API call
- `/actuator/metrics` gives 401 without a token, 403 for USER and 200 for ADMIN

## Troubleshooting
`FileNotFoundException: .../AbstractHealthIndicator.class ... does not exist` when starting from IntelliJ means the
IDE hasn't loaded the actuator dependency yet. Use **Gradle tool window → Reload All Gradle Projects** and run again.
