package kr.co.pennyway.infra.config;

import kr.co.pennyway.infra.client.coordinator.CoordinatorService;
import kr.co.pennyway.infra.client.coordinator.DefaultCoordinatorService;
import kr.co.pennyway.infra.common.importer.PennywayInfraConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

public class DistributedCoordinationConfig implements PennywayInfraConfig {
    private final String chatServerUrl;

    public DistributedCoordinationConfig(@Value("${distributed-coordination.chat-server.url}") String chatServerUrl) {
        this.chatServerUrl = chatServerUrl;
    }

    @Bean
    public CoordinatorService defaultCoordinatorService() {
        return new DefaultCoordinatorService(chatServerUrl);
    }
}
