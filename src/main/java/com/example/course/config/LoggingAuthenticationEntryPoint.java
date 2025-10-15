package com.example.course.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;

/**
 * Logs 401 responses emitted by Spring Security's authentication entry point.
 */
public class LoggingAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private static final Logger log = LoggerFactory.getLogger(LoggingAuthenticationEntryPoint.class);
    private static final String LOG_PREFIX = "[LoggingAuthenticationEntryPoint]";
    private final AuthenticationEntryPoint delegate;

    public LoggingAuthenticationEntryPoint(AuthenticationEntryPoint delegate) {
        this.delegate = delegate;
    }

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {
        String requestId = MDC.get("X-Request-Id");
        log.warn("{} 401 응답 트리거 requestId={} method={} path={} message={}",
                LOG_PREFIX, requestId, request.getMethod(), request.getRequestURI(), authException.getMessage());
        delegate.commence(request, response, authException);
        if (response.getStatus() == HttpStatus.UNAUTHORIZED.value()) {
            log.info("{} 401 응답 전송 완료 requestId={} status=401", LOG_PREFIX, requestId);
        }
    }
}
