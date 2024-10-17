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
import kr.co.pennyway.socket.common.contants.StompNativeHeaderFields;
import kr.co.pennyway.socket.common.exception.InterceptorErrorCode;
import kr.co.pennyway.socket.common.exception.InterceptorErrorException;
import kr.co.pennyway.socket.common.interceptor.marker.ConnectCommandHandler;
import kr.co.pennyway.socket.common.security.authenticate.UserPrincipal;
import kr.co.pennyway.socket.common.security.jwt.AccessTokenClaimKeys;
import kr.co.pennyway.socket.common.security.jwt.AccessTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;

import java.security.Principal;
import java.time.LocalDateTime;

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

        authenticateUser(accessor, userId, expiresDate);
        activateUserSession(userId, accessor.getMessageHeaders());
    }

    private String extractAccessToken(StompHeaderAccessor accessor) {
        String authorization = accessor.getFirstNativeHeader(AuthConstants.AUTHORIZATION.getValue());

        if ((authorization == null || !authorization.startsWith(AuthConstants.TOKEN_TYPE.getValue()))) {
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

    private void activateUserSession(Long userId, MessageHeaders headers) {
        MultiValueMap<String, String> nativeHeaders = headers.get(StompHeaderAccessor.NATIVE_HEADERS, MultiValueMap.class);
        String deviceId, deviceName;

        if (nativeHeaders.containsKey(StompNativeHeaderFields.DEVICE_ID.getValue()) && nativeHeaders.containsKey(StompNativeHeaderFields.DEVICE_NAME.getValue())) {
            deviceId = nativeHeaders.getFirst(StompNativeHeaderFields.DEVICE_ID.getValue());
            deviceName = nativeHeaders.getFirst(StompNativeHeaderFields.DEVICE_NAME.getValue());
            log.debug("[인증 핸들러] 연결 기기 정보 deviceId: {}, deviceName: {}", deviceId, deviceName);
        } else {
            throw new InterceptorErrorException(InterceptorErrorCode.INAVLID_HEADER);
        }

        if (userSessionService.isExists(userId, deviceId)) {
            log.info("[인증 핸들러] 사용자 세션을 업데이트합니다. userId: {}, deviceId: {}", userId, deviceId);
            userSessionService.updateUserStatus(userId, deviceId, UserStatus.ACTIVE_APP);
        } else {
            log.info("[인증 핸들러] 사용자 세션을 생성합니다. userId: {}, deviceId: {}", userId, deviceId);
            userSessionService.create(userId, deviceId, UserSession.of(deviceName));
        }
    }
}
