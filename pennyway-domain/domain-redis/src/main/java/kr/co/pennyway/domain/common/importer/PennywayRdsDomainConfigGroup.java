package kr.co.pennyway.domain.common.importer;

import kr.co.pennyway.domain.config.LettuceConfig;
import kr.co.pennyway.domain.config.RedisConfig;
import kr.co.pennyway.domain.config.RedissonConfig;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PennywayRdsDomainConfigGroup {
    REDIS(RedisConfig.class),
    REDISSON(RedissonConfig.class),
    LETTUCE(LettuceConfig.class);

    private final Class<? extends PennywayRdsDomainConfig> configClass;
}
