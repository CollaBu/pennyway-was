package kr.co.pennyway.infra.client.coordinator;

import java.util.Map;

public interface CoordinatorService {
    /**
     * 채팅 서버에 연결하려는 클라이언트에게 유효한 채팅 서버의 URL을 반환합니다.
     * 이 메서드는 다양한 방식으로 구현될 수 있습니다.
     * 예를 들어, 단일 채팅 서버 환경에서는 고정된 채팅 서버 URL을 반환할 수 있습니다.
     * 분산 채팅 서버 환경이라면, 분산 코디네이션과 같은 서비스를 통해 사용자의 지리적 위치, 채팅 서버의 부하 상태 등을 고려하여 적절한 채팅 서버 URL을 반환할 수 있습니다.
     */
    WebSocket.ChatServerUrl readChatServerUrl(Map<String, String> headers, Object payload);
}
