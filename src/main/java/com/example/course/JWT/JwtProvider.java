package com.example.course.jwt;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Getter
@Slf4j
@Component
public class JwtProvider {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String LOG_PREFIX = "[JwtProvider]";

    private final JwtDecoder jwtDecoder;

    private String resolveToken(String authorizationHeader) {
        if (!StringUtils.hasText(authorizationHeader) || !authorizationHeader.startsWith(BEARER_PREFIX)) {
            log.warn("{} Authorization 헤더가 비어있거나 Bearer 형식이 아님", LOG_PREFIX);
            throw new IllegalArgumentException("Authorization header must start with 'Bearer '");
        }
        log.debug("{} Authorization 헤더 형식 검증 완료", LOG_PREFIX);
        return authorizationHeader.substring(BEARER_PREFIX.length());
    }

    public JwtProvider(@Value("${jwt.secret}") String secret) {
        if (!StringUtils.hasText(secret)) {
            throw new IllegalArgumentException("JWT secret must not be blank");
        }
        SecretKey key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        this.jwtDecoder = NimbusJwtDecoder.withSecretKey(key)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
    }

    public String extractCoupleId(Jwt jwt) {
        return getClaimAsString(jwt, "coupleId");
    }

    public String extractCoupleId(String authorizationHeader) {
        String token = resolveToken(authorizationHeader);
        return getCoupleIdFromToken(token);
    }

    public String getCoupleIdFromToken(String token) {
        return getClaimAsString(token, "coupleId");
    }

    private String getClaimAsString(String token, String claimName) {
        try {
            Jwt jwt = jwtDecoder.decode(token);
            log.info("{} JWT 디코딩 성공 claim={} 포함여부={}", LOG_PREFIX, claimName, jwt.getClaims().containsKey(claimName));
            return getClaimAsString(jwt, claimName);
        } catch (JwtException ex) {
            log.warn("{} JWT 디코딩 실패 message={}", LOG_PREFIX, ex.getMessage());
            throw new IllegalArgumentException("Invalid JWT token", ex);
        }
    }

    private String getClaimAsString(Jwt jwt, String claimName) {
        Object claimValue = jwt.getClaims().get(claimName);
        if (claimValue instanceof String stringValue) {
            String trimmed = stringValue.trim();
            if (StringUtils.hasText(trimmed)) {
                return trimmed;
            }
            log.warn("{} JWT claim이 비어 있음 claim={}", LOG_PREFIX, claimName);
        } else if (claimValue instanceof Number number) {
            long converted = number.longValue();
            if (converted > 0) {
                log.warn("{} JWT claim이 문자열이 아닌 숫자 형식 claim={}", LOG_PREFIX, claimName);
                return Long.toString(converted);
            }
            log.warn("{} JWT claim 숫자 값이 0 이하 claim={}", LOG_PREFIX, claimName);
        } else {
            log.warn("{} JWT claim 타입이 예상과 다름 claim={} valueType={}", LOG_PREFIX, claimName,
                    claimValue != null ? claimValue.getClass().getName() : "null");
        }
        throw new IllegalArgumentException(claimName + " claim is missing or blank in JWT");
    }
}

