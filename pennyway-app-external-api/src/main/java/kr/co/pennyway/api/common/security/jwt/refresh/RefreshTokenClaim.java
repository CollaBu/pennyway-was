package kr.co.pennyway.api.common.security.jwt.refresh;

import kr.co.pennyway.infra.common.jwt.JwtClaims;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.Map;

import static kr.co.pennyway.api.common.security.jwt.refresh.RefreshTokenClaimKeys.ROLE;
import static kr.co.pennyway.api.common.security.jwt.refresh.RefreshTokenClaimKeys.USER_ID;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class RefreshTokenClaim implements JwtClaims {
    private final Map<String, ?> claims;

    public static RefreshTokenClaim of(Long userId, String role) {
        Map<String, Object> claims = Map.of(
                USER_ID.getValue(), userId.toString(),
                ROLE.getValue(), role
        );
        return new RefreshTokenClaim(claims);
    }

    @Override
    public Map<String, ?> getClaims() {
        return claims;
    }
}
