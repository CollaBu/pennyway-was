package kr.co.pennyway.domain.common.importer;

import kr.co.pennyway.domain.config.JpaConfig;
import kr.co.pennyway.domain.config.QueryDslConfig;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PennywayRdbDomainConfigGroup {
    JPA(JpaConfig.class),
    QUERY_DSL(QueryDslConfig.class);

    private final Class<? extends PennywayRdbDomainConfig> configClass;
}
