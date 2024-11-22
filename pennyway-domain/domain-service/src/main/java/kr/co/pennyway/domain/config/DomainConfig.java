package kr.co.pennyway.domain.config;

import kr.co.pennyway.domain.common.importer.EnablePennywayRdbDomainConfig;
import kr.co.pennyway.domain.common.importer.EnablePennywayRedisDomainConfig;
import kr.co.pennyway.domain.common.importer.PennywayRdbDomainConfigGroup;
import kr.co.pennyway.domain.common.importer.PennywayRedisDomainConfigGroup;

@EnablePennywayRdbDomainConfig({
        PennywayRdbDomainConfigGroup.QUERY_DSL,
        PennywayRdbDomainConfigGroup.JPA
})
@EnablePennywayRedisDomainConfig({
        PennywayRedisDomainConfigGroup.REDIS,
        PennywayRedisDomainConfigGroup.REDISSON,
        PennywayRedisDomainConfigGroup.LETTUCE
})
public class DomainConfig {
}
