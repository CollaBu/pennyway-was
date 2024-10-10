package kr.co.pennyway.socket.controller;

import kr.co.pennyway.infra.common.exception.JwtErrorCode;
import kr.co.pennyway.infra.common.exception.JwtErrorException;
import kr.co.pennyway.socket.common.annotation.PreAuthorize;
import kr.co.pennyway.socket.common.security.authenticate.UserPrincipal;
import kr.co.pennyway.socket.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Slf4j
@Controller
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @MessageMapping("auth.refresh")
    @PreAuthorize("#principal instanceof T(kr.co.pennyway.socket.common.security.authenticate.UserPrincipal)")
    public void refreshPrincipal(@Header("Authorization") String authorization, Principal principal, StompHeaderAccessor accessor) {
        log.info("refreshPrincipal AccessToken: {}", authorization);

        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new JwtErrorException(JwtErrorCode.EMPTY_ACCESS_TOKEN);
        }
        String token = authorization.substring(7);

        authService.refreshPrincipal(token, (UserPrincipal) principal, accessor);
    }
}
