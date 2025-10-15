package com.example.course.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Adds a request identifier to MDC and logs key authentication-related fields
 * so 401 root causes can be tracked end-to-end.
 */
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);
    private static final String REQUEST_ID_HEADER = "X-Request-Id";
    private static final String LOG_PREFIX = "[RequestLoggingFilter]";

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        String requestId = resolveRequestId(request);
        MDC.put(REQUEST_ID_HEADER, requestId);
        response.setHeader(REQUEST_ID_HEADER, requestId);
        boolean hasAuthorizationHeader = StringUtils.hasText(request.getHeader(HttpHeaders.AUTHORIZATION));
        log.info("{} 요청 수신 method={} path={} requestId={} authorizationHeader존재={}",
                LOG_PREFIX, request.getMethod(), request.getRequestURI(), requestId, hasAuthorizationHeader);
        try {
            filterChain.doFilter(request, response);
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                log.info("{} 인증 완료 requestId={} principal={} authorities={}",
                        LOG_PREFIX, requestId, authentication.getName(), authentication.getAuthorities());
            } else {
                log.info("{} 인증 미수립 requestId={}", LOG_PREFIX, requestId);
            }
        } catch (Exception ex) {
            log.warn("{} 요청 처리 중 예외 발생 method={} path={} requestId={} status={} message={}",
                    LOG_PREFIX, request.getMethod(), request.getRequestURI(), requestId, response.getStatus(), ex.getMessage());
            throw ex;
        } finally {
            log.info("{} 요청 종료 method={} path={} requestId={} status={}",
                    LOG_PREFIX, request.getMethod(), request.getRequestURI(), requestId, response.getStatus());
            MDC.remove(REQUEST_ID_HEADER);
        }
    }

    private String resolveRequestId(HttpServletRequest request) {
        String header = request.getHeader(REQUEST_ID_HEADER);
        if (StringUtils.hasText(header)) {
            return header;
        }
        return UUID.randomUUID().toString();
    }
}
