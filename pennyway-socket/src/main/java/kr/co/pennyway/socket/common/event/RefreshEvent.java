package kr.co.pennyway.socket.common.event;

import kr.co.pennyway.socket.common.dto.ServerSideMessage;
import org.springframework.context.ApplicationEvent;
import org.springframework.messaging.Message;

public class RefreshEvent<T extends ServerSideMessage> extends ApplicationEvent {
    private final Message<T> message;

    private RefreshEvent(Message<T> message) {
        super(message);
        this.message = message;
    }

    public static <T extends ServerSideMessage> RefreshEvent<T> of(Message<T> message) {
        return new RefreshEvent<>(message);
    }

    public Message<T> getMessage() {
        return message;
    }
}
