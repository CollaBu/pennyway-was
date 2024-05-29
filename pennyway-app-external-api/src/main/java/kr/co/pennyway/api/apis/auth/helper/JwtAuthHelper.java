package kr.co.pennyway.api.apis.auth.helper;

import kr.co.pennyway.api.common.annotation.AccessTokenStrategy;
import kr.co.pennyway.api.common.annotation.RefreshTokenStrategy;
import kr.co.pennyway.api.common.security.jwt.JwtClaimsParserUtil;
import kr.co.pennyway.api.common.security.jwt.Jwts;
import kr.co.pennyway.api.common.security.jwt.access.AccessTokenClaim;
import kr.co.pennyway.api.common.security.jwt.refresh.RefreshTokenClaim;
import kr.co.pennyway.api.common.security.jwt.refresh.RefreshTokenClaimKeys;
import kr.co.pennyway.common.annotation.Helper;
import kr.co.pennyway.domain.common.redis.forbidden.ForbiddenTokenService;
import kr.co.pennyway.domain.common.redis.refresh.RefreshToken;
import kr.co.pennyway.domain.common.redis.refresh.RefreshTokenService;
import kr.co.pennyway.domain.domains.user.domain.User;
import kr.co.pennyway.infra.common.exception.JwtErrorCode;
import kr.co.pennyway.infra.common.exception.JwtErrorException;
import kr.co.pennyway.infra.common.jwt.JwtClaims;
import kr.co.pennyway.infra.common.jwt.JwtProvider;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;

import java.time.Duration;
import java.time.LocalDateTime;

@Slf4j
@Helper
public class JwtAuthHelper {
    private final JwtProvider accessTokenProvider;
    private final JwtProvider refreshTokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final ForbiddenTokenService forbiddenTokenService;

    public JwtAuthHelper(
            @AccessTokenStrategy JwtProvider accessTokenProvider,
            @RefreshTokenStrategy JwtProvider refreshTokenProvider,
            RefreshTokenService refreshTokenService,
            ForbiddenTokenService forbiddenTokenService
    ) {
        this.accessTokenProvider = accessTokenProvider;
        this.refreshTokenProvider = refreshTokenProvider;
        this.refreshTokenService = refreshTokenService;
        this.forbiddenTokenService = forbiddenTokenService;
    }

    /**
     * 사용자 정보 기반으로 access token과 refresh token을 생성하는 메서드 <br/>
     * refresh token은 redis에 저장된다.
     *
     * @param user {@link User}
     * @return {@link Jwts}
     */
    public Jwts createToken(User user) {
        String accessToken = accessTokenProvider.generateToken(AccessTokenClaim.of(user.getId(), user.getRole().getType()));
        String refreshToken = refreshTokenProvider.generateToken(RefreshTokenClaim.of(user.getId(), user.getRole().getType()));

        refreshTokenService.save(RefreshToken.of(user.getId(), refreshToken, toSeconds(refreshTokenProvider.getExpiryDate(refreshToken))));
        return Jwts.of(accessToken, refreshToken);
    }

    public Pair<Long, Jwts> refresh(String refreshToken) {
        JwtClaims claims = refreshTokenProvider.getJwtClaimsFromToken(refreshToken);

        Long userId = JwtClaimsParserUtil.getClaimsValue(claims, RefreshTokenClaimKeys.USER_ID.getValue(), Long::parseLong);
        String role = JwtClaimsParserUtil.getClaimsValue(claims, RefreshTokenClaimKeys.ROLE.getValue(), String.class);
        log.debug("refresh token userId : {}, role : {}", userId, role);

        RefreshToken newRefreshToken;
        try {
            newRefreshToken = refreshTokenService.refresh(userId, refreshToken, refreshTokenProvider.generateToken(RefreshTokenClaim.of(userId, role)));
            log.debug("new refresh token : {}", newRefreshToken.getToken());
        } catch (IllegalArgumentException e) {
            throw new JwtErrorException(JwtErrorCode.EXPIRED_TOKEN);
        } catch (IllegalStateException e) {
            throw new JwtErrorException(JwtErrorCode.TAKEN_AWAY_TOKEN);
        }

        String newAccessToken = accessTokenProvider.generateToken(AccessTokenClaim.of(userId, role));
        log.debug("new access token : {}", newAccessToken);

        return Pair.of(userId, Jwts.of(newAccessToken, newRefreshToken.getToken()));
    }

    /**
     * access token과 refresh token을 삭제하여 로그아웃 처리하는 메서드
     *
     * @param refreshToken : 삭제할 refreshToken. null이거나, 기존에 refreshToken이 없을 경우 삭제 과정을 생략한다.
     * @throws JwtErrorException <br/>
     *                           • {@link JwtErrorCode#EXPIRED_TOKEN} : 만료된 access token 삭제하려고 할 경우 <br/>
     *                           • {@link JwtErrorCode#WITHOUT_OWNERSHIP_REFRESH_TOKEN} : 다른 사용자의 refresh token을 삭제하려고 할 경우 <br/>
     *                           • {@link JwtErrorCode#MALFORMED_TOKEN} : refresh token이 유효하지 않을 경우
     */
    public void removeAccessTokenAndRefreshToken(Long userId, String accessToken, String refreshToken) {
        JwtClaims jwtClaims = null;
        if (refreshToken != null) {
            try {
                jwtClaims = refreshTokenProvider.getJwtClaimsFromToken(refreshToken);
            } catch (JwtErrorException e) {
                if (!e.getErrorCode().equals(JwtErrorCode.EXPIRED_TOKEN)) {
                    throw e;
                }
            }
        }

        if (jwtClaims != null) {
            deleteRefreshToken(userId, jwtClaims, refreshToken);
        }

        deleteAccessToken(userId, accessToken);
    }

    private void deleteRefreshToken(Long userId, JwtClaims jwtClaims, String refreshToken) {
        Long refreshTokenUserId = Long.parseLong((String) jwtClaims.getClaims().get(RefreshTokenClaimKeys.USER_ID.getValue()));
        log.info("로그아웃 요청 refresh token id : {}", refreshTokenUserId);

        if (!userId.equals(refreshTokenUserId)) {
            throw new JwtErrorException(JwtErrorCode.WITHOUT_OWNERSHIP_REFRESH_TOKEN);
        }

        try {
            refreshTokenService.delete(refreshTokenUserId, refreshToken);
        } catch (IllegalArgumentException e) {
            log.warn("refresh token not found. id : {}", userId);
        }
    }

    private void deleteAccessToken(Long userId, String accessToken) {
        LocalDateTime expiresAt = accessTokenProvider.getExpiryDate(accessToken);
        log.info("로그아웃 요청 access token expiresAt : {}", expiresAt);
        forbiddenTokenService.createForbiddenToken(accessToken, userId, expiresAt);
    }

    private long toSeconds(LocalDateTime expiryTime) {
        return Duration.between(LocalDateTime.now(), expiryTime).getSeconds();
    }
}
