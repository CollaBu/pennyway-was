package kr.co.pennyway.domain.common.importer;

import kr.co.pennyway.domain.config.LettuceConfig;
import kr.co.pennyway.domain.config.RedisConfig;
import kr.co.pennyway.domain.config.RedissonConfig;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PennywayDomainConfigGroup {
    REDIS_DOMAIN(RedisConfig.class),
    REDISSON_DOMAIN(RedissonConfig.class),
    LETTUCE_DOMAIN(LettuceConfig.class);

    private final Class<? extends PennywayDomainConfig> configClass;
}
