package com.chicamax.sentinella.shared;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.chicamax.sentinella.shared.infrastructure.observability.TraceIdFilter;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class TraceIdFilterTest {

    @Test
    void shouldPreserveIncomingTraceId() throws Exception {
        TraceIdFilter filter = new TraceIdFilter("X-Trace-Id");
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/v1/dashboard/executive");
        request.addHeader("X-Trace-Id", "trace-incoming-123");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertEquals("trace-incoming-123", response.getHeader("X-Trace-Id"));
    }

    @Test
    void shouldGenerateTraceIdWhenMissing() throws Exception {
        TraceIdFilter filter = new TraceIdFilter("X-Trace-Id");
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/v1/reports");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        String traceId = response.getHeader("X-Trace-Id");
        assertNotNull(traceId);
        assertFalse(traceId.isBlank());
    }
}
