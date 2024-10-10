package kr.co.pennyway.socket.common.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.co.pennyway.socket.common.dto.ServerSideMessage;
import kr.co.pennyway.socket.common.util.StompMessageUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.AbstractSubscribableChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReceiptEventHandler {
    private final ObjectMapper objectMapper;

    @Bean
    @Async
    public CompletableFuture<ApplicationListener<SubscribeEvent<ServerSideMessage>>> subscribeEventListener(final AbstractSubscribableChannel clientOutboundChannel) {
        return CompletableFuture.completedFuture(event -> {
            log.info("subscribeEventListener: {}", event);
            Message<ServerSideMessage> message = event.getMessage();
            StompHeaderAccessor accessor = StompHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

            Message<byte[]> payload = StompMessageUtil.createMessage(accessor, message.getPayload(), objectMapper);

            sendReceiptMessage(clientOutboundChannel, accessor, payload.getPayload());
        });
    }

    @Bean
    @Async
    @EventListener
    public CompletableFuture<ApplicationListener<SessionSubscribeEvent>> sessionSubscribeEventListener(final AbstractSubscribableChannel clientOutboundChannel) {
        return CompletableFuture.completedFuture(event -> {
            log.info("sessionSubscribeEventListener: {}", event);
            Message<byte[]> message = event.getMessage();
            StompHeaderAccessor accessor = StompHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

            Message<byte[]> payload = StompMessageUtil.createMessage(accessor, ServerSideMessage.of("2000", "success subscribe"), objectMapper);

            sendReceiptMessage(clientOutboundChannel, accessor, payload.getPayload());
        });
    }

    private void sendReceiptMessage(AbstractSubscribableChannel clientOutboundChannel, StompHeaderAccessor accessor, byte[] payload) {
        if (accessor != null && accessor.getReceipt() != null) {
            accessor.setHeader("stompCommand", StompCommand.RECEIPT);
            accessor.setReceiptId(accessor.getReceipt());

            Message<byte[]> receiptMessage = MessageBuilder.createMessage(payload, accessor.getMessageHeaders());
            log.info("receiptMessage: {}", receiptMessage);

            clientOutboundChannel.send(receiptMessage);
        }
    }
}
