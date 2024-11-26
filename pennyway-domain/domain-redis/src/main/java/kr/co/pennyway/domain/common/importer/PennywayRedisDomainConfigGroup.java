package kr.co.pennyway.domain.common.importer;

import kr.co.pennyway.domain.config.RedissonConfig;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PennywayRedisDomainConfigGroup {
    REDISSON_INFRA(RedissonConfig.class);

    private final Class<? extends PennywayRedisDomainConfig> configClass;
}
