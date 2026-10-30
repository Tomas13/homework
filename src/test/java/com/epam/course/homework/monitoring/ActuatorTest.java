package com.epam.course.homework.monitoring;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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
class ActuatorTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void healthIsPublicButHidesDetails() throws Exception {
		mockMvc.perform(get("/actuator/health"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("UP"))
				.andExpect(jsonPath("$.components.productCatalog.status").value("UP"))
				.andExpect(jsonPath("$.components.productCatalog.details").doesNotExist());
	}

	@Test
	void healthShowsCustomIndicatorDetailsToAdmin() throws Exception {
		mockMvc.perform(get("/actuator/health").with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.components.productCatalog.details.products").isNumber())
				.andExpect(jsonPath("$.components.productCatalog.details.outOfStock").isNumber())
				.andExpect(jsonPath("$.components.tokenSigningKey.status").value("UP"))
				.andExpect(jsonPath("$.components.tokenSigningKey.details.keySize").value(2048));
	}

	@Test
	void prometheusExposesCustomMetrics() throws Exception {
		mockMvc.perform(post("/api/products")
						.with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"name\":\"Keyboard\",\"price\":79.50,\"quantity\":0}"))
				.andExpect(status().isCreated());

		mockMvc.perform(get("/actuator/prometheus"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("products_operations_total{application=\"homework\",operation=\"create\"}")))
				.andExpect(content().string(containsString("products_catalog_size{")))
				.andExpect(content().string(containsString("products_out_of_stock{")))
				.andExpect(content().string(containsString("auth_logins_total{")));
	}

	@Test
	void otherEndpointsRequireAdmin() throws Exception {
		mockMvc.perform(get("/actuator/metrics"))
				.andExpect(status().isUnauthorized());
		mockMvc.perform(get("/actuator/metrics").with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
				.andExpect(status().isForbidden());
		mockMvc.perform(get("/actuator/metrics").with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
				.andExpect(status().isOk());
	}
}
