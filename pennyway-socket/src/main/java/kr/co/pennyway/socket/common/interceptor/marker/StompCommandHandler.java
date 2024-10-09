package kr.co.pennyway.socket.common.interceptor.marker;

import org.springframework.messaging.Message;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;

/**
 * STOMP 명령어 핸들러 인터페이스
 */
public interface StompCommandHandler {
    /**
     * 해당 핸들러가 지원하는 명령어인지 확인한다.
     *
     * @param command {@link StompCommand} 명령어
     */
    boolean isSupport(StompCommand command);

    void handle(Message<?> message, StompHeaderAccessor accessor);
}

