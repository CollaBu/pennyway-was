package kr.co.pennyway.infra.common.importer;

import kr.co.pennyway.infra.config.DistributedCoordinationConfig;
import kr.co.pennyway.infra.config.FcmConfig;
import kr.co.pennyway.infra.config.GuidConfig;
import kr.co.pennyway.infra.config.MessageBrokerConfig;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PennywayInfraConfigGroup {
    FCM(FcmConfig.class),
    DISTRIBUTED_COORDINATION_CONFIG(DistributedCoordinationConfig.class),
    MESSAGE_BROKER_CONFIG(MessageBrokerConfig.class),
    GUID_GENERATOR_CONFIG(GuidConfig.class);

    private final Class<? extends PennywayInfraConfig> configClass;
}
