#!/bin/bash
# Runs the OAuth 2 authorization code flow (with PKCE) the way a browser would and prints the JWT access token.
#
# Usage:   scripts/get-token.sh <username> <password>
# Example: TOKEN=$(scripts/get-token.sh admin admin)
#          curl -H "Authorization: Bearer $TOKEN" http://127.0.0.1:8080/api/products
#
# Set PORT to talk to a server that is not on 8080 (the registered redirect URI stays on 8080).
set -e

BASE=http://127.0.0.1:${PORT:-8080}
REDIRECT=http://127.0.0.1:8080/authorized
JAR=$(mktemp)
trap 'rm -f "$JAR"' EXIT

# PKCE: random code_verifier and its SHA-256 code_challenge (base64url, no padding)
VERIFIER=$(openssl rand -base64 48 | tr -d '=+/\n' | cut -c1-64)
CHALLENGE=$(printf '%s' "$VERIFIER" | openssl dgst -sha256 -binary | openssl base64 | tr '+/' '-_' | tr -d '=\n')

AUTHORIZE="$BASE/oauth2/authorize?response_type=code&client_id=product-client&scope=products&redirect_uri=$REDIRECT&code_challenge=$CHALLENGE&code_challenge_method=S256"

# 1. Start the flow; not logged in yet, so the server remembers the request and sends us to /login
curl -s -c "$JAR" -b "$JAR" -o /dev/null "$AUTHORIZE"

# 2. Log in with the form (needs the CSRF token from the login page)
CSRF=$(curl -s -c "$JAR" -b "$JAR" "$BASE/login" | grep -oE 'name="_csrf"[^>]*value="[^"]+"' | grep -oE 'value="[^"]+"' | cut -d'"' -f2)
curl -s -c "$JAR" -b "$JAR" -o /dev/null \
	--data-urlencode "username=$1" --data-urlencode "password=$2" --data-urlencode "_csrf=$CSRF" "$BASE/login"

# 3. Authorize again as the logged-in user; the redirect carries the authorization code
CODE=$(curl -s -c "$JAR" -b "$JAR" -o /dev/null -w '%{redirect_url}' "$AUTHORIZE" | grep -oE 'code=[^&]+' | cut -d= -f2)
if [ -z "$CODE" ]; then
	echo "Login failed: no authorization code returned" >&2
	exit 1
fi

# 4. Exchange the code for tokens (client authenticates with HTTP Basic, PKCE verifier proves we started the flow)
curl -s -u product-client:secret -X POST "$BASE/oauth2/token" \
	-d grant_type=authorization_code -d "redirect_uri=$REDIRECT" \
	--data-urlencode "code=$CODE" --data-urlencode "code_verifier=$VERIFIER" \
	| grep -oE '"access_token":"[^"]+"' | cut -d'"' -f4
