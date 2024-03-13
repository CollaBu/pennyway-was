package kr.co.pennyway.common.security.jwt.access;

import kr.co.infra.common.jwt.JwtClaims;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class AccessTokenClaim implements JwtClaims {
    private static final String USER_ID = "userId";
    private static final String ROLE = "role";
    private final Map<String, ?> claims;

    public static AccessTokenClaim of(String userId, String role) {
        Map<String, Object> claims = Map.of(
                USER_ID, userId,
                ROLE, role
        );
        return new AccessTokenClaim(claims);
    }

    @Override
    public Map<String, ?> getClaims() {
        return claims;
    }

    public String getUserId() {
        return (String) claims.get(USER_ID);
    }

    public String getRole() {
        return (String) claims.get(ROLE);
    }
}
