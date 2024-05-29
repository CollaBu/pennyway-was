package kr.co.pennyway.infra.common.importer;

import kr.co.pennyway.infra.config.FcmConfig;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PennywayInfraConfigGroup {
    FCM(FcmConfig.class);

    private final Class<? extends PennywayInfraConfig> configClass;
}
