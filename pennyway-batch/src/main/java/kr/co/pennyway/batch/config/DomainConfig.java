package kr.co.pennyway.batch.config;

import kr.co.pennyway.domain.common.importer.EnablePennywayRedisDomainConfig;
import kr.co.pennyway.domain.common.importer.PennywayRedisDomainConfigGroup;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnablePennywayRedisDomainConfig(value = {
        PennywayRedisDomainConfigGroup.REDIS_INFRA,
        PennywayRedisDomainConfigGroup.LETTUCE_INFRA
})
public class DomainConfig {
}
