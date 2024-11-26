package kr.co.pennyway.domain.common.importer;

import kr.co.pennyway.domain.config.LettuceDomainConfig;
import kr.co.pennyway.domain.config.RedisDomainConfig;
import kr.co.pennyway.domain.config.RedissonDomainConfig;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PennywayDomainConfigGroup {
    REDIS_DOMAIN(RedisDomainConfig.class),
    REDISSON_DOMAIN(RedissonDomainConfig.class),
    LETTUCE_DOMAIN(LettuceDomainConfig.class);

    private final Class<? extends PennywayDomainConfig> configClass;
}
