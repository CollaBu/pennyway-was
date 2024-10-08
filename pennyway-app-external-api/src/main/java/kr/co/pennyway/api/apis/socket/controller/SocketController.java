package kr.co.pennyway.api.apis.socket.controller;

import kr.co.pennyway.api.apis.socket.api.SocketApi;
import kr.co.pennyway.api.apis.socket.usecase.SocketUseCase;
import kr.co.pennyway.api.common.response.SuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v2/socket")
public class SocketController implements SocketApi {
    private static final String CHAT_SERVER_URL = "chatServerUrl";
    private final SocketUseCase socketUseCase;

    @Override
    @GetMapping("/chat")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getChatServerInfo() {
        return ResponseEntity.ok(SuccessResponse.from(CHAT_SERVER_URL, socketUseCase.getChatServerUrl()));
    }
}
