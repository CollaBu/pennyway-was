package kr.co.pennyway.socket.common.security.jwt;

import kr.co.pennyway.infra.common.jwt.JwtClaims;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class AccessTokenClaim implements JwtClaims {
    private final Map<String, ?> claims;

    public static AccessTokenClaim of(Long userId, String role) {
        Map<String, Object> claims = Map.of(
                AccessTokenClaimKeys.USER_ID.getValue(), userId.toString(),
                AccessTokenClaimKeys.ROLE.getValue(), role
        );
        return new AccessTokenClaim(claims);
    }

    @Override
    public Map<String, ?> getClaims() {
        return claims;
    }
}
