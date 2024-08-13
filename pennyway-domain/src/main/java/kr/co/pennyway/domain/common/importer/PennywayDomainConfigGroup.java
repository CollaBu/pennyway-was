package kr.co.pennyway.domain.common.importer;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PennywayDomainConfigGroup {
    ;
    

    private final Class<? extends PennywayDomainConfig> configClass;
}
