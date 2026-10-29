package com.epam.course.homework.controller;

import com.epam.course.homework.model.Product;
import com.epam.course.homework.monitoring.ProductMetrics;
import com.epam.course.homework.repository.ProductRepository;
import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products")
public class ProductController {

	private final ProductRepository productRepository;
	private final ProductMetrics productMetrics;

	public ProductController(ProductRepository productRepository, ProductMetrics productMetrics) {
		this.productRepository = productRepository;
		this.productMetrics = productMetrics;
	}

	@PostMapping
	public ResponseEntity<Product> create(@RequestBody ProductRequest request) {
		Product saved = productRepository.save(
				new Product(request.name(), request.description(), request.price(), request.quantity()));
		productMetrics.recordOperation("create");
		return ResponseEntity.created(URI.create("/api/products/" + saved.getId())).body(saved);
	}

	@GetMapping
	public List<Product> findAll() {
		return productRepository.findAll();
	}

	@GetMapping("/{id}")
	public ResponseEntity<Product> findById(@PathVariable Long id) {
		return ResponseEntity.of(productRepository.findById(id));
	}

	@PutMapping("/{id}")
	public ResponseEntity<Product> update(@PathVariable Long id, @RequestBody ProductRequest request) {
		return ResponseEntity.of(productRepository.findById(id).map(product -> {
			product.setName(request.name());
			product.setDescription(request.description());
			product.setPrice(request.price());
			product.setQuantity(request.quantity());
			productMetrics.recordOperation("update");
			return productRepository.save(product);
		}));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable Long id) {
		if (!productRepository.existsById(id)) {
			return ResponseEntity.notFound().build();
		}
		productRepository.deleteById(id);
		productMetrics.recordOperation("delete");
		return ResponseEntity.noContent().build();
	}
}
