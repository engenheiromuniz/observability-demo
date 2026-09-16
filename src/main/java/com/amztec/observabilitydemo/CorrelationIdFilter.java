package com.amztec.observabilitydemo;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;

import java.io.IOException;
import java.util.UUID;

/**
 * Garante que toda requisição tenha um Correlation ID, e também injeta
 * traceId/spanId (gerados pelo Micrometer Tracing) no MDC, para que
 * apareçam junto no log JSON — correlacionando logs com traces.
 *
 * Não é mais um @Component/@WebFilter direto: é registrado explicitamente
 * pela classe FilterConfig, com uma ordem que garante que ele rode DEPOIS
 * do filtro de tracing do Spring Boot — só assim o span já existe quando
 * chamamos tracer.currentSpan() aqui dentro.
 */
public class CorrelationIdFilter implements jakarta.servlet.Filter {

    private static final String HEADER_NAME = "X-Correlation-Id";
    private static final String MDC_KEY = "correlationId";

    private final Tracer tracer;

    public CorrelationIdFilter(Tracer tracer) {
        this.tracer = tracer;
    }

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

        Span currentSpan = tracer.currentSpan();
        if (currentSpan != null) {
            MDC.put("traceId", currentSpan.context().traceId());
            MDC.put("spanId", currentSpan.context().spanId());
        }

        try {
            chain.doFilter(request, response);
        } finally {
            // Essencial: limpar o MDC no final, senão o valor "vaza" para
            // a próxima requisição que reutilizar essa mesma thread.
            MDC.remove(MDC_KEY);
            MDC.remove("traceId");
            MDC.remove("spanId");
        }
    }
}