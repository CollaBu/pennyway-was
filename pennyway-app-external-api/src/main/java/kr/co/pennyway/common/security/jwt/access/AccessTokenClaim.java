package kr.co.pennyway.common.security.jwt.access;

import kr.co.infra.common.jwt.JwtClaims;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.Map;

import static kr.co.pennyway.common.security.jwt.access.AccessTokenClaimKeys.ROLE;
import static kr.co.pennyway.common.security.jwt.access.AccessTokenClaimKeys.USER_ID;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class AccessTokenClaim implements JwtClaims {
    private final Map<String, ?> claims;

    public static AccessTokenClaim of(Long userId, String role) {
        Map<String, Object> claims = Map.of(
                USER_ID.getValue(), userId.toString(),
                ROLE.getValue(), role
        );
        return new AccessTokenClaim(claims);
    }

    @Override
    public Map<String, ?> getClaims() {
        return claims;
    }
}
