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
        Long userId = jwtAuthHelper.getClaimsValue(claims, AccessTokenClaimKeys.USER_ID.getValue(), Long::parseLong);

        log.info("auth_id {} 사용자는 로그인 중입니다.", userId);

        return AuthStateDto.of(userId);
    }

    public void signOut(Long userId, String authHeader, String refreshToken) {
        jwtAuthHelper.removeAccessTokenAndRefreshToken(userId, authHeader, refreshToken);
    }
}
