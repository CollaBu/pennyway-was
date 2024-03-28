package kr.co.pennyway.api.apis.auth.helper;

import kr.co.pennyway.api.common.annotation.AccessTokenStrategy;
import kr.co.pennyway.api.common.annotation.RefreshTokenStrategy;
import kr.co.pennyway.api.common.security.jwt.Jwts;
import kr.co.pennyway.api.common.security.jwt.access.AccessTokenClaim;
import kr.co.pennyway.api.common.security.jwt.refresh.RefreshTokenClaim;
import kr.co.pennyway.api.common.security.jwt.refresh.RefreshTokenClaimKeys;
import kr.co.pennyway.common.annotation.Helper;
import kr.co.pennyway.domain.common.redis.refresh.RefreshToken;
import kr.co.pennyway.domain.common.redis.refresh.RefreshTokenService;
import kr.co.pennyway.domain.domains.user.domain.User;
import kr.co.pennyway.infra.common.jwt.JwtProvider;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Helper
public class JwtAuthHelper {
    private final JwtProvider accessTokenProvider;
    private final JwtProvider refreshTokenProvider;
    private final RefreshTokenService refreshTokenService;

    public JwtAuthHelper(
            @AccessTokenStrategy JwtProvider accessTokenProvider,
            @RefreshTokenStrategy JwtProvider refreshTokenProvider,
            RefreshTokenService refreshTokenService
    ) {
        this.accessTokenProvider = accessTokenProvider;
        this.refreshTokenProvider = refreshTokenProvider;
        this.refreshTokenService = refreshTokenService;
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
        Map<String, ?> claims = refreshTokenProvider.getJwtClaimsFromToken(refreshToken).getClaims();

        Long userId = Long.parseLong((String) claims.get(RefreshTokenClaimKeys.USER_ID.getValue()));
        String role = (String) claims.get(RefreshTokenClaimKeys.ROLE.getValue());

        String newAccessToken = accessTokenProvider.generateToken(AccessTokenClaim.of(userId, role));
        RefreshToken newRefreshToken = refreshTokenService.refresh(userId, refreshToken, refreshTokenProvider.generateToken(RefreshTokenClaim.of(userId, role)));

        return Pair.of(userId, Jwts.of(newAccessToken, newRefreshToken.getToken()));
    }

    private long toSeconds(LocalDateTime expiryTime) {
        return Duration.between(LocalDateTime.now(), expiryTime).getSeconds();
    }
}
