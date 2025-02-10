package kr.co.pennyway.api.apis.socket.usecase;

import kr.co.pennyway.api.apis.socket.service.ChatServerSearchService;
import kr.co.pennyway.common.annotation.UseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UseCase
@RequiredArgsConstructor
public class SocketUseCase {
    private final ChatServerSearchService chatServerSearchService;

    public String getChatServerUrl() {
        return chatServerSearchService.getChatServerUrl();
    }
}
