package kr.co.pennyway.socket.controller;

import kr.co.pennyway.socket.common.annotation.PreAuthorize;
import kr.co.pennyway.socket.common.dto.StatusMessage;
import kr.co.pennyway.socket.common.security.authenticate.UserPrincipal;
import kr.co.pennyway.socket.service.StatusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
@RequiredArgsConstructor
public class StatusController {
    private final StatusService statusService;

    @MessageMapping("status.me")
    @PreAuthorize("#isAuthenticated(#principal)")
    public void updateStatus(UserPrincipal principal, StatusMessage message, StompHeaderAccessor accessor) {
        statusService.updateStatus(principal.getUserId(), principal.getDeviceId(), message, accessor);
    }
}
