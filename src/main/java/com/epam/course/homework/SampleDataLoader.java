package com.epam.course.homework;

import com.epam.course.homework.model.Product;
import com.epam.course.homework.repository.ProductRepository;
import java.math.BigDecimal;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class SampleDataLoader implements CommandLineRunner {

	private final ProductRepository productRepository;

	public SampleDataLoader(ProductRepository productRepository) {
		this.productRepository = productRepository;
	}

	@Override
	public void run(String... args) {
		productRepository.saveAll(List.of(
				new Product("Laptop", "14-inch ultrabook", new BigDecimal("999.99"), 5),
				new Product("Mouse", "Wireless mouse", new BigDecimal("24.90"), 50),
				new Product("Laptop Stand", "Aluminium stand", new BigDecimal("39.00"), 12)));

		System.out.println("All products:");
		productRepository.findAll().forEach(System.out::println);

		System.out.println("Products matching 'laptop':");
		productRepository.findByNameContainingIgnoreCase("laptop").forEach(System.out::println);
	}
}
