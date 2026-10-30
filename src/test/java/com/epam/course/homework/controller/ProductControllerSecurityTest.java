package com.epam.course.homework.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class ProductControllerSecurityTest {

	private static final String PRODUCT_JSON =
			"{\"name\":\"Keyboard\",\"description\":\"Mechanical\",\"price\":79.50,\"quantity\":20}";

	@Autowired
	private MockMvc mockMvc;

	@Test
	void rejectsRequestsWithoutToken() throws Exception {
		mockMvc.perform(get("/api/products"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void userCanReadProducts() throws Exception {
		mockMvc.perform(get("/api/products").with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
				.andExpect(status().isOk());
	}

	@Test
	void userCannotCreateProducts() throws Exception {
		mockMvc.perform(post("/api/products")
						.with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER")))
						.contentType(MediaType.APPLICATION_JSON)
						.content(PRODUCT_JSON))
				.andExpect(status().isForbidden());
	}

	@Test
	void userCannotDeleteProducts() throws Exception {
		mockMvc.perform(delete("/api/products/1").with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
				.andExpect(status().isForbidden());
	}

	@Test
	void adminCanCreateProducts() throws Exception {
		mockMvc.perform(post("/api/products")
						.with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
						.contentType(MediaType.APPLICATION_JSON)
						.content(PRODUCT_JSON))
				.andExpect(status().isCreated());
	}
}
