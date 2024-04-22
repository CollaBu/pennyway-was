package kr.co.pennyway.api.apis.auth.usecase;

import kr.co.pennyway.api.apis.auth.helper.JwtAuthHelper;
import kr.co.pennyway.common.annotation.UseCase;
import kr.co.pennyway.infra.common.exception.JwtErrorCode;
import kr.co.pennyway.infra.common.exception.JwtErrorException;
import kr.co.pennyway.infra.common.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UseCase
@RequiredArgsConstructor
public class UserAuthUseCase {
    private final JwtAuthHelper jwtAuthHelper;
    private final JwtProvider accessTokenProvider;

    public boolean isSignIn(String accessToken) {
        if (accessTokenProvider.isTokenExpired(accessToken))
            throw new JwtErrorException(JwtErrorCode.EXPIRED_TOKEN);

        return true;
    }

    public void signOut(Long userId, String authHeader, String refreshToken) {
        jwtAuthHelper.removeAccessTokenAndRefreshToken(userId, authHeader, refreshToken);
    }
}
