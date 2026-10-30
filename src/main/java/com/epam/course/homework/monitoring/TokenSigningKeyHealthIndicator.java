package com.epam.course.homework.monitoring;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKMatcher;
import com.nimbusds.jose.jwk.JWKSelector;
import com.nimbusds.jose.jwk.KeyType;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import java.util.List;

import org.springframework.boot.health.contributor.AbstractHealthIndicator;
import org.springframework.boot.health.contributor.Health;
import org.springframework.stereotype.Component;

/**
 * Shown as "tokenSigningKey" in /actuator/health. DOWN when there is no RSA private key to sign JWTs with,
 * because then no user can get an access token.
 */
@Component
public class TokenSigningKeyHealthIndicator extends AbstractHealthIndicator {

	private static final JWKSelector RSA_SIGNING_KEYS =
			new JWKSelector(new JWKMatcher.Builder().keyType(KeyType.RSA).privateOnly(true).build());

	private final JWKSource<SecurityContext> jwkSource;

	public TokenSigningKeyHealthIndicator(JWKSource<SecurityContext> jwkSource) {
		super("Token signing key health check failed");
		this.jwkSource = jwkSource;
	}

	@Override
	protected void doHealthCheck(Health.Builder builder) throws Exception {
		List<JWK> keys = jwkSource.get(RSA_SIGNING_KEYS, null);
		if (keys.isEmpty()) {
			builder.down().withDetail("reason", "No RSA signing key available");
			return;
		}
		builder.up()
				.withDetail("keyId", keys.getFirst().getKeyID())
				.withDetail("keySize", keys.getFirst().size());
	}
}
