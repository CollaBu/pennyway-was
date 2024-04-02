package kr.co.pennyway.infra.common.importer;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PennywayInfraConfigGroup {
    ;

    private final Class<? extends PennywayInfraConfig> configClass;
}
