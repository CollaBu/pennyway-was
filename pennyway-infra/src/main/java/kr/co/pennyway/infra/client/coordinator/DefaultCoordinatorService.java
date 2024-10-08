package kr.co.pennyway.infra.client.coordinator;

import java.util.Map;

/**
 * 이 클래스는 단일 채팅 서버 환경에서 사용할 수 있는 기본적인 {@link CoordinatorService} 구현체입니다.
 * 미리 정의된 채팅 서버 URL을 반환합니다.
 */
public class DefaultCoordinatorService implements CoordinatorService {
    private final String chatServerUrl;

    public DefaultCoordinatorService(String chatServerUrl) {
        this.chatServerUrl = chatServerUrl;
    }

    @Override
    public WebSocket.ChatServerUrl readChatServerUrl(Map<String, String> headers, Object payload) {
        return WebSocket.ChatServerUrl.of(chatServerUrl);
    }
}
