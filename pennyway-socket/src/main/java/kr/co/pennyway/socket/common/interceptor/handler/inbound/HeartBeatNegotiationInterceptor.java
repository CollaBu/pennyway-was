package kr.co.pennyway.socket.common.interceptor.handler.inbound;

import kr.co.pennyway.socket.common.interceptor.marker.ConnectCommandHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class HeartBeatNegotiationInterceptor implements ConnectCommandHandler {
    private static final String HEART_BEAT_HEADER = "heart-beat";

    private static final long SERVER_HEARTBEAT_SEND = 25000;    // sx
    private static final long SERVER_HEARTBEAT_RECEIVE = 25000; // sy

    @Override
    public boolean isSupport(StompCommand command) {
        return StompCommand.CONNECT.equals(command);
    }

    @Override
    public void handle(Message<?> message, StompHeaderAccessor accessor) {
        String heartbeat = accessor.getFirstNativeHeader(HEART_BEAT_HEADER);

        if (heartbeat != null) {
            String[] parts = heartbeat.split(",");
            
            if (parts.length == 2) {
                long cx = Long.parseLong(parts[0]);
                long cy = Long.parseLong(parts[1]);

                long clientToServer = (cx != 0 && SERVER_HEARTBEAT_RECEIVE != 0)
                        ? Math.max(cx, SERVER_HEARTBEAT_RECEIVE) : 0;
                long serverToClient = (SERVER_HEARTBEAT_SEND != 0 && cy != 0)
                        ? Math.max(SERVER_HEARTBEAT_SEND, cy) : 0;

                log.info("Heart-beat negotiation - Client wants: {}, Server wants: {}",
                        heartbeat, SERVER_HEARTBEAT_SEND + "," + SERVER_HEARTBEAT_RECEIVE);
                log.info("Negotiated heart-beat - Client to Server: {}, Server to Client: {}",
                        clientToServer, serverToClient);

                accessor.setNativeHeader(HEART_BEAT_HEADER,
                        clientToServer + "," + serverToClient);
            }
        }
    }
}
