package kr.co.pennyway.domain.context.account.service;

import kr.co.pennyway.common.annotation.DomainService;
import kr.co.pennyway.domain.domains.forbidden.service.ForbiddenTokenRedisService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

@Slf4j
@DomainService
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class ForbiddenTokenService {
    private final ForbiddenTokenRedisService forbiddenTokenRedisService;

    public void createForbiddenToken(String accessToken, Long userId, LocalDateTime expiresAt) {
        forbiddenTokenRedisService.createForbiddenToken(accessToken, userId, expiresAt);
    }

    public boolean isForbidden(String accessToken) {
        return forbiddenTokenRedisService.isForbidden(accessToken);
    }
}
