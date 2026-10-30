package com.epam.course.homework.monitoring;

import com.epam.course.homework.repository.ProductRepository;

import org.springframework.boot.health.contributor.AbstractHealthIndicator;
import org.springframework.boot.health.contributor.Health;
import org.springframework.stereotype.Component;

/**
 * Shown as "productCatalog" in /actuator/health. DOWN when the catalog can't be queried
 * (e.g. the database is unavailable); otherwise UP with catalog statistics.
 */
@Component
public class ProductCatalogHealthIndicator extends AbstractHealthIndicator {

	static final int LOW_STOCK_THRESHOLD = 5;

	private final ProductRepository productRepository;

	public ProductCatalogHealthIndicator(ProductRepository productRepository) {
		super("Product catalog health check failed");
		this.productRepository = productRepository;
	}

	@Override
	protected void doHealthCheck(Health.Builder builder) {
		builder.up()
				.withDetail("products", productRepository.count())
				.withDetail("outOfStock", productRepository.countByQuantityLessThanEqual(0))
				.withDetail("lowStock", productRepository.countByQuantityLessThanEqual(LOW_STOCK_THRESHOLD))
				.withDetail("lowStockThreshold", LOW_STOCK_THRESHOLD);
	}
}
