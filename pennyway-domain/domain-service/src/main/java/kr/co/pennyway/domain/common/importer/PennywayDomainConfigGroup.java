package kr.co.pennyway.domain.common.importer;

import kr.co.pennyway.domain.config.RedissonDomainConfig;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PennywayDomainConfigGroup {
    REDISSON_DOMAIN(RedissonDomainConfig.class);

    private final Class<? extends PennywayDomainConfig> configClass;
}
