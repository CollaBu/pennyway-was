package kr.co.pennyway.api.apis.users.usecase;

import kr.co.pennyway.common.annotation.UseCase;
import kr.co.pennyway.domain.common.redis.forbidden.ForbiddenTokenService;
import kr.co.pennyway.domain.common.redis.refresh.RefreshTokenService;
import kr.co.pennyway.infra.common.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

@Slf4j
@UseCase
@RequiredArgsConstructor
public class UserAuthUseCase {
    private final JwtProvider accessTokenProvider;
    private final ForbiddenTokenService forbiddenTokenService;
    private final RefreshTokenService refreshTokenService;

    public void signOut(Long userId, String accessToken, String refreshToken) {
        LocalDateTime expiresAt = accessTokenProvider.getExpiryDate(accessToken);
        forbiddenTokenService.createForbiddenToken(accessToken, userId, expiresAt);
        refreshTokenService.delete(userId, refreshToken);
    }
}
