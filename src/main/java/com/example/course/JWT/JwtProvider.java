package com.example.course.jwt;

import lombok.Getter;
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
@Component
public class JwtProvider {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtDecoder jwtDecoder;

    private String resolveToken(String authorizationHeader) {
        if (!StringUtils.hasText(authorizationHeader) || !authorizationHeader.startsWith(BEARER_PREFIX)) {
            throw new IllegalArgumentException("Authorization header must start with 'Bearer '");
        }
        return authorizationHeader.substring(BEARER_PREFIX.length());
    }

    public JwtProvider(@Value("${jwt.secret}") String secret) {
        if (!StringUtils.hasText(secret)) {
            throw new IllegalArgumentException("JWT secret must not be blank");
        }
        SecretKey key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        // 필터가 헤더에서 토큰 문자열을 꺼내 JwtProvider의 NimbusJwtDecoder로 검증·디코딩하고 검증에 성공하면 Jwt 객체를 만들고 이를 인증 객체를 (JwtAuthenticationToken)에 담아 SecurityContext에 주입
        this.jwtDecoder = NimbusJwtDecoder.withSecretKey(key)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
        // 컨트롤러 메서드에 @AuthenticationPrincipal Jwt jwt라는 파라미터가 있으면, 방금 SecurityContext에 저장된 Jwt 객체가 그대로 주입됨
    }

    public Long extractCoupleId(Jwt jwt) {
        return getClaimAsLong(jwt, "coupleId");
    }

    public Long extractCoupleId(String authorizationHeader) {
        String token = resolveToken(authorizationHeader);
        return getCoupleIdFromToken(token);
    }

    public Long getCoupleIdFromToken(String token) {
        return getClaimAsLong(token, "coupleId");
    }

    private Long getClaimAsLong(String token, String claimName) {
        try {
            Jwt jwt = jwtDecoder.decode(token);
            return getClaimAsLong(jwt, claimName);
        } catch (JwtException ex) {
            throw new IllegalArgumentException("Invalid JWT token", ex);
        }
    }

    private Long getClaimAsLong(Jwt jwt, String claimName) {
        Object claimValue = jwt.getClaims().get(claimName);
        if (claimValue instanceof Number number) {
            return number.longValue();
        }
        if (claimValue instanceof String stringValue && StringUtils.hasText(stringValue)) {
            try {
                return Long.parseLong(stringValue);
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException(claimName + " claim must be numeric", ex);
            }
        }
        throw new IllegalArgumentException(claimName + " claim is missing in JWT");
    }


}
