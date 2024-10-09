package kr.co.pennyway.socket.common.interceptor;

import kr.co.pennyway.socket.common.interceptor.marker.StompCommandHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class StompCommandHandlerFactory {
    private final Map<StompCommand, List<StompCommandHandler>> handlers = new EnumMap<>(StompCommand.class);

    @Autowired
    public StompCommandHandlerFactory(List<StompCommandHandler> allHandlers) {
        allHandlers.forEach(this::registerHandler);
        log.info("StompCommandHandlerFactory: handlers={}", handlers);
    }

    private void registerHandler(StompCommandHandler handler) {
        Arrays.stream(StompCommand.values())
                .filter(handler::isSupport)
                .forEach(command -> {
                    handlers.computeIfAbsent(command, k -> new ArrayList<>()).add(handler);
                    log.info("Registered handler {} for command {}", handler.getClass().getSimpleName(), command);
                });
    }

    public List<StompCommandHandler> getHandlers(StompCommand command) {
        return handlers.getOrDefault(command, List.of());
    }
}
