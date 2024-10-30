package kr.co.pennyway.socket.common.interceptor.handler.inbound;

import kr.co.pennyway.domain.common.redis.session.UserSession;
import kr.co.pennyway.domain.common.redis.session.UserSessionService;
import kr.co.pennyway.domain.common.redis.session.UserStatus;
import kr.co.pennyway.domain.domains.user.domain.User;
import kr.co.pennyway.domain.domains.user.service.UserService;
import kr.co.pennyway.infra.common.exception.JwtErrorCode;
import kr.co.pennyway.infra.common.exception.JwtErrorException;
import kr.co.pennyway.infra.common.jwt.AuthConstants;
import kr.co.pennyway.infra.common.jwt.JwtClaims;
import kr.co.pennyway.infra.common.util.JwtClaimsParserUtil;
import kr.co.pennyway.socket.common.constants.StompNativeHeaderFields;
import kr.co.pennyway.socket.common.exception.InterceptorErrorCode;
import kr.co.pennyway.socket.common.exception.InterceptorErrorException;
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
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ConnectAuthenticateHandler implements ConnectCommandHandler {
    private final AccessTokenProvider accessTokenProvider;
    private final UserService userService;
    private final UserSessionService userSessionService;

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

        existsHeader(accessor);

        UserPrincipal principal = (UserPrincipal) authenticateUser(accessor, userId, expiresDate);
        activateUserSession(principal);
    }

    private String extractAccessToken(StompHeaderAccessor accessor) {
        String authorization = accessor.getFirstNativeHeader(AuthConstants.AUTHORIZATION.getValue());

        if ((authorization == null || !authorization.startsWith(AuthConstants.TOKEN_TYPE.getValue()))) {
            log.warn("[인증 핸들러] 헤더에 Authorization이 없거나 Bearer 토큰이 아닙니다.");
            throw new JwtErrorException(JwtErrorCode.EMPTY_ACCESS_TOKEN);
        }

        return authorization.substring(7);
    }

    private void existsHeader(StompHeaderAccessor accessor) {
        List<String> headerNames = List.of(
                StompNativeHeaderFields.DEVICE_ID.getValue(),
                StompNativeHeaderFields.DEVICE_NAME.getValue()
        );

        for (String headerName : headerNames) {
            if (!accessor.containsNativeHeader(headerName)) {
                log.warn("[인증 핸들러] 헤더에 {}가 없습니다.", headerName);
                throw new InterceptorErrorException(InterceptorErrorCode.INAVLID_HEADER);
            }
        }
    }

    private Principal authenticateUser(StompHeaderAccessor accessor, Long userId, LocalDateTime expiresDate) {
        String deviceId = accessor.getFirstNativeHeader(StompNativeHeaderFields.DEVICE_ID.getValue());
        String deviceName = accessor.getFirstNativeHeader(StompNativeHeaderFields.DEVICE_NAME.getValue());

        User user = userService.readUser(userId)
                .orElseThrow(() -> new JwtErrorException(JwtErrorCode.MALFORMED_TOKEN));
        Principal principal = UserPrincipal.of(user, expiresDate, deviceId, deviceName);

        log.info("[인증 핸들러] 사용자 인증 완료: {}", principal);

        accessor.setUser(principal);

        return principal;
    }

    private void activateUserSession(UserPrincipal principal) {
        if (userSessionService.isExists(principal.getUserId(), principal.getDeviceId())) {
            log.info("[인증 핸들러] 사용자 세션을 갱신합니다. userId: {}, deviceId: {}", principal.getUserId(), principal.getDeviceId());
            userSessionService.updateUserStatus(principal.getUserId(), principal.getDeviceId(), UserStatus.ACTIVE_APP);
            log.info("[인증 핸들러] 사용자 세션을 갱신했습니다. userId: {}, deviceId: {}", principal.getUserId(), principal.getDeviceId());
        } else {
            log.info("[인증 핸들러] 사용자 세션을 생성합니다. userId: {}, deviceId: {}", principal.getUserId(), principal.getDeviceId());
            userSessionService.create(principal.getUserId(), principal.getDeviceId(), UserSession.of(principal.getDeviceId(), principal.getDeviceName()));
            log.info("[인증 핸들러] 사용자 세션을 생성했습니다. userId: {}, deviceId: {}", principal.getUserId(), principal.getDeviceId());
        }
    }
}
