package kr.co.pennyway.domain.context.account.service;

import kr.co.pennyway.common.annotation.DomainService;
import kr.co.pennyway.domain.domains.refresh.domain.RefreshToken;
import kr.co.pennyway.domain.domains.refresh.service.RefreshTokenRedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@DomainService
@RequiredArgsConstructor
public class RefreshTokenService {
    private final RefreshTokenRedisService refreshTokenRedisService;

    public void create(RefreshToken refreshToken) {
        refreshTokenRedisService.save(refreshToken);
    }

    public RefreshToken refresh(Long userId, String deviceId, String oldRefreshToken, String newRefreshToken) {
        return refreshTokenRedisService.refresh(userId, deviceId, oldRefreshToken, newRefreshToken);
    }

    public void deleteAll(Long userId) {
        refreshTokenRedisService.deleteAll(userId);
    }
}
