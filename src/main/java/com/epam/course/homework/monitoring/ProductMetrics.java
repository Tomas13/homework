package com.epam.course.homework.monitoring;

import com.epam.course.homework.repository.ProductRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;

import org.springframework.stereotype.Component;

/**
 * Custom product metrics, exposed at /actuator/prometheus as:
 * <ul>
 * <li>{@code products_operations_total{operation="create|update|delete"}}: changes made through the REST API</li>
 * <li>{@code products_catalog_size}: number of products in the catalog</li>
 * <li>{@code products_out_of_stock}: number of products with quantity 0</li>
 * </ul>
 */
@Component
public class ProductMetrics {

	private final MeterRegistry meterRegistry;

	public ProductMetrics(MeterRegistry meterRegistry, ProductRepository productRepository) {
		this.meterRegistry = meterRegistry;

		Gauge.builder("products.catalog.size", productRepository, ProductRepository::count)
				.description("Number of products in the catalog")
				.register(meterRegistry);
		Gauge.builder("products.out.of.stock", productRepository, repository -> repository.countByQuantityLessThanEqual(0))
				.description("Number of products with no stock left")
				.register(meterRegistry);
	}

	public void recordOperation(String operation) {
		Counter.builder("products.operations")
				.description("Product changes made through the REST API")
				.tag("operation", operation)
				.register(meterRegistry)
				.increment();
	}
}
