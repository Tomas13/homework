package com.epam.course.homework.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Redirect target of the authorization code flow: shows the code so it can be exchanged for a token with curl.
 */
@RestController
public class AuthorizedController {

	@GetMapping("/authorized")
	public String authorized(@RequestParam String code) {
		return "Authorization code: " + code + "\n\nExchange it for a JWT (use the code_verifier you generated):\n"
				+ "curl -u product-client:secret -X POST http://127.0.0.1:8080/oauth2/token "
				+ "-d grant_type=authorization_code -d redirect_uri=http://127.0.0.1:8080/authorized "
				+ "-d code_verifier=<your code_verifier> -d code=" + code;
	}
}
