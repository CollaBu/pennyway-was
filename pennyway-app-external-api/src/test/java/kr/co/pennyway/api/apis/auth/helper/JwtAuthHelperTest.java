package kr.co.pennyway.api.apis.auth.helper;

import kr.co.pennyway.api.common.security.jwt.Jwts;
import kr.co.pennyway.api.common.security.jwt.access.AccessTokenProvider;
import kr.co.pennyway.api.common.security.jwt.refresh.RefreshTokenClaim;
import kr.co.pennyway.api.common.security.jwt.refresh.RefreshTokenProvider;
import kr.co.pennyway.domain.context.account.service.ForbiddenTokenService;
import kr.co.pennyway.domain.context.account.service.RefreshTokenService;
import kr.co.pennyway.domain.domains.refresh.domain.RefreshToken;
import kr.co.pennyway.domain.domains.user.type.Role;
import kr.co.pennyway.infra.common.exception.JwtErrorCode;
import kr.co.pennyway.infra.common.exception.JwtErrorException;
import kr.co.pennyway.infra.common.jwt.JwtClaims;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@Slf4j
@ExtendWith(MockitoExtension.class)
public class JwtAuthHelperTest {
    private JwtAuthHelper jwtAuthHelper;

    @Mock
    private AccessTokenProvider accessTokenProvider;

    @Mock
    private RefreshTokenProvider refreshTokenProvider;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private ForbiddenTokenService forbiddenTokenService;

    @BeforeEach
    public void setUp() {
        jwtAuthHelper = new JwtAuthHelper(accessTokenProvider, refreshTokenProvider, refreshTokenService, forbiddenTokenService);
    }

    @Test
    @DisplayName("사용자 아이디에 해당하는 리프레시 토큰이 존재할 시, 리프레시 토큰 갱신에 성공한다.")
    public void RefreshTokenRefreshSuccess() {
        // given
        Long userId = 1L;
        String deviceId = "AA-BBB-CC-DDD";
        String oldRefreshToken = "refreshToken";
        String newRefreshToken = "newRefreshToken";
        String newAccessToken = "newAccessToken";

        JwtClaims claims = RefreshTokenClaim.of(userId, deviceId, Role.USER.getType());

        given(refreshTokenProvider.getJwtClaimsFromToken(oldRefreshToken)).willReturn(claims);
        given(accessTokenProvider.generateToken(any())).willReturn(newAccessToken);
        given(refreshTokenProvider.generateToken(any())).willReturn(newRefreshToken);
        given(refreshTokenService.refresh(eq(userId), eq(deviceId), eq(oldRefreshToken), eq(newRefreshToken)))
                .willReturn(RefreshToken.builder()
                        .userId(userId)
                        .deviceId(deviceId)
                        .token(newRefreshToken)
                        .ttl(1000L)
                        .build());

        // when
        Pair<Long, Jwts> result = jwtAuthHelper.refresh(oldRefreshToken);

        // then
        assertEquals(userId, result.getLeft(), "사용자 아이디가 일치하지 않습니다.");
        assertEquals(newAccessToken, result.getRight().accessToken(), "갱신된 액세스 토큰이 일치하지 않습니다.");
        assertEquals(newRefreshToken, result.getRight().refreshToken(), "리프레시 토큰이 갱신되지 않았습니다.");

        verify(refreshTokenService).refresh(eq(userId), eq(deviceId), eq(oldRefreshToken), eq(newRefreshToken));
        verify(accessTokenProvider).generateToken(any());
        verify(refreshTokenProvider).generateToken(any());
    }

    @Test
    @DisplayName("사용자 아이디에 해당하는 다른 리프레시 토큰이 저장되어 있을 시, 탈취되었다고 판단하고 토큰을 제거한 후 JwtErrorException을 발생시킨다.")
    public void RefreshTokenRefreshFail() {
        // given
        Long userId = 1L;
        String deviceId = "AA-BBB-CC-DDD";
        String oldRefreshToken = "anotherRefreshToken";
        String newRefreshToken = "newRefreshToken";

        JwtClaims claims = RefreshTokenClaim.of(userId, deviceId, Role.USER.toString());
        given(refreshTokenProvider.getJwtClaimsFromToken(oldRefreshToken)).willReturn(claims);
        given(refreshTokenProvider.generateToken(any())).willReturn(newRefreshToken);
        given(refreshTokenService.refresh(eq(userId), eq(deviceId), eq(oldRefreshToken), eq(newRefreshToken)))
                .willThrow(new IllegalStateException("Token taken away"));

        // when & then
        JwtErrorException exception = assertThrows(JwtErrorException.class, () -> jwtAuthHelper.refresh(oldRefreshToken));
        assertEquals(JwtErrorCode.TAKEN_AWAY_TOKEN, exception.getErrorCode());

        verify(refreshTokenService).refresh(eq(userId), eq(deviceId), eq(oldRefreshToken), eq(newRefreshToken));
        verify(refreshTokenProvider).generateToken(any());
    }
}
