package kr.co.pennyway.domain.config;

import kr.co.pennyway.domain.common.importer.EnablePennywayRedisDomainConfig;
import kr.co.pennyway.domain.common.importer.PennywayRedisDomainConfigGroup;

@EnablePennywayRedisDomainConfig({
        PennywayRedisDomainConfigGroup.REDIS,
        PennywayRedisDomainConfigGroup.REDISSON,
        PennywayRedisDomainConfigGroup.LETTUCE
})
public class DomainConfig {
}
