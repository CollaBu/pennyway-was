package kr.co.pennyway.api.apis.auth.usecase;

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

    public boolean isSignIn(String authHeader) {
        String accessToken = accessTokenProvider.resolveToken(authHeader);
        log.debug("accessToken: {}", accessToken);

        if (accessToken.isBlank())
            return false;

        JwtClaims claims = accessTokenProvider.getJwtClaimsFromToken(accessToken);
        log.debug("auth_id: {}", claims.getClaims().get(AccessTokenClaimKeys.USER_ID.getValue()));

        return true;
    }

    public void signOut(Long userId, String authHeader, String refreshToken) {
        jwtAuthHelper.removeAccessTokenAndRefreshToken(userId, authHeader, refreshToken);
    }
}
