package kr.co.pennyway.socket.common.interceptor.handler;

import kr.co.pennyway.domain.domains.user.domain.User;
import kr.co.pennyway.domain.domains.user.service.UserService;
import kr.co.pennyway.infra.common.exception.JwtErrorCode;
import kr.co.pennyway.infra.common.exception.JwtErrorException;
import kr.co.pennyway.infra.common.jwt.JwtClaims;
import kr.co.pennyway.infra.common.util.JwtClaimsParserUtil;
import kr.co.pennyway.socket.common.interceptor.marker.ConnectCommandHandler;
import kr.co.pennyway.socket.common.security.authenticate.UserPrincipal;
import kr.co.pennyway.socket.common.security.jwt.AccessTokenClaimKeys;
import kr.co.pennyway.socket.common.security.jwt.AccessTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class ConnectAuthenticateHandler implements ConnectCommandHandler {
    private final AccessTokenProvider accessTokenProvider;
    private final UserService userService;

    @Override
    public boolean isSupport(StompCommand command) {
        return StompCommand.CONNECT.equals(command);
    }

    @Override
    public void handle(Message<?> message, StompHeaderAccessor accessor) {
        String accessToken = extractAccessToken(accessor);

        JwtClaims claims = accessTokenProvider.getJwtClaimsFromToken(accessToken);
        Long userId = JwtClaimsParserUtil.getClaimsValue(claims, AccessTokenClaimKeys.USER_ID.getValue(), Long::parseLong);
        LocalDateTime expiresDate = accessTokenProvider.getExpiryDate(accessToken);

        authenticateUser(accessor, userId, expiresDate);
    }

    private String extractAccessToken(StompHeaderAccessor accessor) {
        String authorization = accessor.getFirstNativeHeader("Authorization");

        if ((authorization == null || !authorization.startsWith("Bearer "))) {
            log.warn("[인증 핸들러] 헤더에 Authorization이 없거나 Bearer 토큰이 아닙니다.");
            throw new JwtErrorException(JwtErrorCode.EMPTY_ACCESS_TOKEN);
        }

        return authorization.substring(7);
    }

    private void authenticateUser(StompHeaderAccessor accessor, Long userId, LocalDateTime expiresDate) {
        User user = userService.readUser(userId)
                .orElseThrow(() -> new JwtErrorException(JwtErrorCode.MALFORMED_TOKEN));
        Principal principal = UserPrincipal.from(user, expiresDate);

        log.info("[인증 핸들러] 사용자 인증 완료: {}", principal);

        accessor.setUser(principal);
    }
}
