package kr.co.pennyway.api.apis.socket.usecase;

import kr.co.pennyway.common.annotation.UseCase;
import kr.co.pennyway.infra.client.coordinator.CoordinatorService;
import kr.co.pennyway.infra.client.coordinator.WebSocket;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UseCase
@RequiredArgsConstructor
public class SocketUseCase {
    private final CoordinatorService defaultCoordinatorService;

    public String getChatServerUrl() {
        WebSocket.ChatServerUrl response = defaultCoordinatorService.readChatServerUrl(null, null);

        return response.url();
    }
}
