package com.minierp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.config.PageableHandlerMethodArgumentResolverCustomizer;

import com.minierp.common.PaginationDefaults;

/**
 * Web MVC settings. Pagination bounds come from {@link PaginationDefaults} so
 * the resolver and the use cases share one source of truth.
 */
@Configuration
public class WebConfig {

    @Bean
    PageableHandlerMethodArgumentResolverCustomizer pageableCustomizer() {
        return resolver -> {
            resolver.setFallbackPageable(
                    PageRequest.of(PaginationDefaults.DEFAULT_PAGE, PaginationDefaults.DEFAULT_SIZE));
            resolver.setMaxPageSize(PaginationDefaults.MAX_SIZE);
        };
    }
}
