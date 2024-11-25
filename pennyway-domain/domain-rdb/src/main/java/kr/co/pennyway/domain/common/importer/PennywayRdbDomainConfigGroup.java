package kr.co.pennyway.domain.common.importer;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PennywayRdbDomainConfigGroup {
    ;

    private final Class<? extends PennywayRdbDomainConfig> configClass;
}
