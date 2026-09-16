package com.amztec.observabilitydemo;

import io.micrometer.tracing.Tracer;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean<CorrelationIdFilter> correlationIdFilter(Tracer tracer) {
        FilterRegistrationBean<CorrelationIdFilter> registration =
                new FilterRegistrationBean<>(new CorrelationIdFilter(tracer));
        registration.addUrlPatterns("/*");

        // LOWEST_PRECEDENCE = roda por último entre os filtros registrados
        // pelo Spring, ou seja, depois que o filtro de tracing do Spring
        // Boot (que roda bem cedo, com prioridade alta) já iniciou o span.
        registration.setOrder(Ordered.LOWEST_PRECEDENCE);
        return registration;
    }
}