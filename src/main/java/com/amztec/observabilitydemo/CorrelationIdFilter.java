package com.amztec.observabilitydemo;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

/**
 * Garante que toda requisição tenha um Correlation ID:
 * - Se o cliente já mandou um header X-Correlation-Id (comum quando a
 *   chamada vem de outro microsserviço), reaproveita ele.
 * - Se não, gera um novo (UUID).
 * O valor é colocado no MDC do SLF4J, então TODO log dentro dessa
 * requisição — não importa em qual classe — automaticamente ganha esse
 * campo, sem precisar passar ele manualmente por parâmetro em lugar nenhum.
 */
@Component
@WebFilter("/*")
public class CorrelationIdFilter implements jakarta.servlet.Filter {

    private static final String HEADER_NAME = "X-Correlation-Id";
    private static final String MDC_KEY = "correlationId";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String correlationId = httpRequest.getHeader(HEADER_NAME);
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }

        // Devolve o ID também na resposta, para quem chamou conseguir
        // rastrear a chamada do outro lado, se precisar.
        httpResponse.setHeader(HEADER_NAME, correlationId);

        MDC.put(MDC_KEY, correlationId);
        try {
            chain.doFilter(request, response);
        } finally {
            // Essencial: limpar o MDC no final, senão o valor "vaza" para
            // a próxima requisição que reutilizar essa mesma thread.
            MDC.remove(MDC_KEY);
        }
    }
}