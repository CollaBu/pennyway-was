package kr.co.pennyway.batch.config;

import kr.co.pennyway.domain.common.importer.EnablePennywayRdbDomainConfig;
import kr.co.pennyway.domain.common.importer.EnablePennywayRedisDomainConfig;
import kr.co.pennyway.domain.common.importer.PennywayRdbDomainConfigGroup;
import kr.co.pennyway.domain.common.importer.PennywayRedisDomainConfigGroup;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnablePennywayRedisDomainConfig(value = {
        PennywayRedisDomainConfigGroup.REDIS,
        PennywayRedisDomainConfigGroup.LETTUCE
})
@EnablePennywayRdbDomainConfig(value = {
        PennywayRdbDomainConfigGroup.JPA,
        PennywayRdbDomainConfigGroup.QUERY_DSL
})
public class DomainConfig {
}
