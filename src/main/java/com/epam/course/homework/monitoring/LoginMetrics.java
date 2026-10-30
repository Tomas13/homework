package com.epam.course.homework.monitoring;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;

import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

/**
 * Counts login attempts on the login page, exposed as {@code auth_logins_total{result="success|failure"}}.
 * Only username/password logins are counted, not the JWT checks done on every API request.
 */
@Component
public class LoginMetrics {

	private final Counter successfulLogins;
	private final Counter failedLogins;

	public LoginMetrics(MeterRegistry meterRegistry) {
		this.successfulLogins = Counter.builder("auth.logins")
				.description("Login attempts with username and password")
				.tag("result", "success")
				.register(meterRegistry);
		this.failedLogins = Counter.builder("auth.logins")
				.description("Login attempts with username and password")
				.tag("result", "failure")
				.register(meterRegistry);
	}

	@EventListener
	public void onSuccess(AuthenticationSuccessEvent event) {
		if (event.getAuthentication() instanceof UsernamePasswordAuthenticationToken) {
			successfulLogins.increment();
		}
	}

	@EventListener
	public void onFailure(AbstractAuthenticationFailureEvent event) {
		if (event.getAuthentication() instanceof UsernamePasswordAuthenticationToken) {
			failedLogins.increment();
		}
	}
}
