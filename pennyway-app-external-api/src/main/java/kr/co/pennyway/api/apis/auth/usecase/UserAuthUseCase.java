package kr.co.pennyway.api.apis.auth.usecase;

import kr.co.pennyway.api.apis.auth.dto.AuthStateDto;
import kr.co.pennyway.api.apis.auth.helper.JwtAuthHelper;
import kr.co.pennyway.api.common.security.jwt.access.AccessTokenClaimKeys;
import kr.co.pennyway.common.annotation.UseCase;
import kr.co.pennyway.infra.common.jwt.JwtClaims;
import kr.co.pennyway.infra.common.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UseCase
@RequiredArgsConstructor
public class UserAuthUseCase {
    private final JwtAuthHelper jwtAuthHelper;
    private final JwtProvider accessTokenProvider;

    public AuthStateDto isSignIn(String authHeader) {
        String accessToken = accessTokenProvider.resolveToken(authHeader);

        JwtClaims claims = accessTokenProvider.getJwtClaimsFromToken(accessToken);
        Long userId = jwtAuthHelper.getClaimValue(claims, AccessTokenClaimKeys.USER_ID.getValue(), Long.class);

        log.debug("auth_id: {}", claims.getClaims().get(AccessTokenClaimKeys.USER_ID.getValue()));

        return AuthStateDto.of(userId);
    }

    public void signOut(Long userId, String authHeader, String refreshToken) {
        jwtAuthHelper.removeAccessTokenAndRefreshToken(userId, authHeader, refreshToken);
    }
}
