package kr.co.pennyway.socket.service;

import kr.co.pennyway.infra.common.exception.JwtErrorException;
import kr.co.pennyway.socket.common.dto.ServerSideMessage;
import kr.co.pennyway.socket.common.event.RefreshEvent;
import kr.co.pennyway.socket.common.security.authenticate.UserPrincipal;
import kr.co.pennyway.socket.common.security.jwt.AccessTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    private final AccessTokenProvider accessTokenProvider;
    private final ApplicationEventPublisher eventPublisher;

    public void refreshPrincipal(String token, UserPrincipal principal, StompHeaderAccessor accessor) {
        Message<ServerSideMessage> message;

        try {
            LocalDateTime expiresAt = accessTokenProvider.getExpiryDate(token);
            principal.updateExpiresAt(expiresAt);

            ServerSideMessage payload = ServerSideMessage.of("2000", "토큰 갱신 성공");
            message = MessageBuilder.createMessage(payload, accessor.getMessageHeaders());
        } catch (JwtErrorException e) {
            log.info("refresh failed: {}", e.getErrorCode().getExplainError());

            ServerSideMessage payload = ServerSideMessage.of(e.causedBy().getCode(), e.getErrorCode().getExplainError());
            message = MessageBuilder.createMessage(payload, accessor.getMessageHeaders());
        }

        eventPublisher.publishEvent(RefreshEvent.of(message));
    }
}
