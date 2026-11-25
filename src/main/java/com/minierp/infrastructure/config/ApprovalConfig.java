package com.minierp.infrastructure.config;

import java.math.BigDecimal;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.minierp.domain.service.ApprovalPolicy;

/**
 * Wires the pure {@link ApprovalPolicy} domain service from configuration.
 */
@Configuration
public class ApprovalConfig {

    @Bean
    ApprovalPolicy approvalPolicy(MinierpProperties properties) {
        return new ApprovalPolicy(BigDecimal.valueOf(properties.approval().thresholdAmount()));
    }
}
