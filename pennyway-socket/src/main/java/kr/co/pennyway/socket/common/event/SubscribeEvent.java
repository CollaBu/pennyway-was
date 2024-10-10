package kr.co.pennyway.socket.common.event;

import kr.co.pennyway.socket.common.dto.ServerSideMessage;
import org.springframework.context.ApplicationEvent;
import org.springframework.messaging.Message;

public class SubscribeEvent<T extends ServerSideMessage> extends ApplicationEvent {
    private final Message<T> message;

    private SubscribeEvent(Message<T> message) {
        super(message);
        this.message = message;
    }

    public static <T extends ServerSideMessage> SubscribeEvent<T> of(Message<T> message) {
        return new SubscribeEvent<>(message);
    }

    public Message<T> getMessage() {
        return message;
    }
}