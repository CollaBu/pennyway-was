package kr.co.pennyway.socket.common.interceptor.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.co.pennyway.socket.common.dto.ServerSideMessage;
import kr.co.pennyway.socket.common.interceptor.marker.StompExceptionHandler;
import kr.co.pennyway.socket.common.util.StompMessageUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;

/**
 * STOMP 예외 처리를 위한 추상 기본 클래스.
 * 이 클래스는 공통적인 예외 처리 로직을 제공하며, 구체적인 예외 처리 동작은 하위 클래스에서 구현합니다.
 */
@Slf4j
public abstract class AbstractStompExceptionHandler implements StompExceptionHandler {
    protected final ObjectMapper objectMapper;

    public AbstractStompExceptionHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Message<byte[]> handle(Message<byte[]> clientMessage, Throwable cause) {
        if (isNullReturnRequired(clientMessage)) {
            return null;
        }

        StompHeaderAccessor accessor = StompHeaderAccessor.create(getStompCommand());
        accessor.setLeaveMutable(true);
        extractClientHeaderAccessor(clientMessage, accessor);
        ServerSideMessage payload = getServerSideMessage(cause);

        if (payload != null) {
            accessor.setMessage(payload.code());
        }

        accessor.setImmutable();
        return StompMessageUtil.createMessage(accessor, payload, objectMapper);
    }

    /**
     * 클라이언트 메시지의 유효성을 검사합니다.
     * 기본 구현은 항상 false를 반환합니다. 필요한 경우 하위 클래스에서 재정의할 수 있습니다.
     *
     * @param clientMessage 클라이언트로부터 받은 원본 메시지
     * @return null을 반환해야 한다면 true, 그렇지 않다면 false
     */
    protected boolean isNullReturnRequired(Message<byte[]> clientMessage) {
        return false;
    }

    /**
     * 이 핸들러가 사용할 STOMP 명령을 반환합니다.
     *
     * @return STOMP 명령
     */
    protected abstract StompCommand getStompCommand();

    /**
     * 주어진 예외를 기반으로 {@link ServerSideMessage}를 생성합니다.
     *
     * @param cause 발생한 예외
     * @return 생성된 ServerSideMessage
     */
    protected abstract ServerSideMessage getServerSideMessage(Throwable cause);
}