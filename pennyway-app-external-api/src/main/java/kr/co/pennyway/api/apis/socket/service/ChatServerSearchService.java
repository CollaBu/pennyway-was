package kr.co.pennyway.api.apis.socket.service;

import kr.co.pennyway.infra.client.coordinator.CoordinatorService;
import kr.co.pennyway.infra.client.coordinator.WebSocket;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatServerSearchService {
    private final CoordinatorService defaultCoordinatorService;

    public String getChatServerUrl() {
        WebSocket.ChatServerUrl response = defaultCoordinatorService.readChatServerUrl(null, null);

        return response.url();
    }
}
