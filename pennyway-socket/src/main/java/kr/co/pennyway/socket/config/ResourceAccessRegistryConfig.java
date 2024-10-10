package kr.co.pennyway.socket.config;

import kr.co.pennyway.socket.common.registry.ChatRoomAccessChecker;
import kr.co.pennyway.socket.common.registry.ResourceAccessRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class ResourceAccessRegistryConfig {
    private final ChatRoomAccessChecker chatRoomChecker;

    @Bean
    public ResourceAccessRegistry configureResourceAccess() {
        ResourceAccessRegistry registry = new ResourceAccessRegistry();

        registry.registerChecker("^/sub/chat\\.room\\.\\d+$", chatRoomChecker);

        return registry;
    }
}