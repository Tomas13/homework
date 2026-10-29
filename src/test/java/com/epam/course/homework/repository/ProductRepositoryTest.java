package com.epam.course.homework.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.epam.course.homework.model.Product;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

@DataJpaTest
class ProductRepositoryTest {

	@Autowired
	private ProductRepository productRepository;

	@Test
	void savesProductAndGeneratesId() {
		Product saved = productRepository.save(new Product("Laptop", "14-inch ultrabook", new BigDecimal("999.99"), 5));

		assertThat(saved.getId()).isNotNull();
		assertThat(productRepository.findById(saved.getId())).contains(saved);
	}

	@Test
	void findsProductsByNameIgnoringCase() {
		productRepository.saveAll(List.of(
				new Product("Laptop", null, new BigDecimal("999.99"), 5),
				new Product("Mouse", null, new BigDecimal("24.90"), 50)));

		List<Product> result = productRepository.findByNameContainingIgnoreCase("LAP");

		assertThat(result).extracting(Product::getName).containsExactly("Laptop");
	}
}
