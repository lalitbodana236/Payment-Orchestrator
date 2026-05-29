package com.yuno.assignment.config;

import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

@Configuration
public class RetryConfig {

    @Bean
    RetryTemplate providerRetryTemplate(PaymentOrchestrationProperties properties) {
        PaymentOrchestrationProperties.Retry retry = properties.retry();

        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(retry.initialIntervalMs());
        backOffPolicy.setMultiplier(retry.multiplier());
        backOffPolicy.setMaxInterval(retry.maxIntervalMs());

        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy(
                retry.maxAttempts(),
                Map.of(
                        com.yuno.assignment.exception.TransientProviderException.class, true,
                        com.yuno.assignment.exception.ProviderTimeoutException.class, true),
                true);

        RetryTemplate template = new RetryTemplate();
        template.setBackOffPolicy(backOffPolicy);
        template.setRetryPolicy(retryPolicy);
        return template;
    }
}
