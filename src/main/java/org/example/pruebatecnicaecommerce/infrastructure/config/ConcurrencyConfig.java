package org.example.pruebatecnicaecommerce.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Configuration for concurrency management features
 */
@Configuration
@EnableRetry
@EnableAsync
@EnableTransactionManagement
public class ConcurrencyConfig {
    // Configuration is provided through annotations
    // Additional beans can be added here if needed
}