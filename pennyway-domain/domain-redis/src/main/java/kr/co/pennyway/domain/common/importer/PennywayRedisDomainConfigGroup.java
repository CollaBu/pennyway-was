package kr.co.pennyway.domain.common.importer;

import kr.co.pennyway.domain.config.LettuceConfig;
import kr.co.pennyway.domain.config.RedisConfig;
import kr.co.pennyway.domain.config.RedissonConfig;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PennywayRedisDomainConfigGroup {
    REDIS_INFRA(RedisConfig.class),
    REDISSON_INFRA(RedissonConfig.class),
    LETTUCE_INFRA(LettuceConfig.class);

    private final Class<? extends PennywayRedisDomainConfig> configClass;
}
