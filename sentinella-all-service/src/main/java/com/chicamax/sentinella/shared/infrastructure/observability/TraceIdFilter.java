package com.chicamax.sentinella.shared.infrastructure.observability;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class TraceIdFilter extends OncePerRequestFilter {

    private static final String MDC_TRACE_ID = "traceId";

    private final String traceHeader;

    public TraceIdFilter(@Value("${observability.trace-header:X-Trace-Id}") String traceHeader) {
        this.traceHeader = traceHeader;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String traceId = resolveTraceId(request);
        MDC.put(MDC_TRACE_ID, traceId);
        response.setHeader(traceHeader, traceId);
        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(MDC_TRACE_ID);
        }
    }

    private String resolveTraceId(HttpServletRequest request) {
        String incoming = request.getHeader(traceHeader);
        if (incoming != null && !incoming.isBlank()) {
            return incoming;
        }
        return UUID.randomUUID().toString();
    }
}
