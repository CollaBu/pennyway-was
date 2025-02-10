package kr.co.pennyway.socket.common.interceptor.handler.inbound;

import kr.co.pennyway.domain.context.account.service.UserSessionService;
import kr.co.pennyway.domain.domains.session.type.UserStatus;
import kr.co.pennyway.socket.common.interceptor.marker.DisconnectCommandHandler;
import kr.co.pennyway.socket.common.security.authenticate.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DisconnectHandler implements DisconnectCommandHandler {
    private final UserSessionService userSessionService;

    @Override
    public boolean isSupport(StompCommand command) {
        return StompCommand.DISCONNECT.equals(command);
    }

    @Override
    public void handle(Message<?> message, StompHeaderAccessor accessor) {
        UserPrincipal principal = (UserPrincipal) accessor.getUser();

        userSessionService.updateUserStatus(principal.getUserId(), principal.getDeviceId(), UserStatus.INACTIVE);
    }
}
