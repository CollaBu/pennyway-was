package kr.co.pennyway.api.config;

import kr.co.pennyway.domain.common.importer.EnablePennywayRedisDomainConfig;
import kr.co.pennyway.domain.common.importer.PennywayRedisDomainConfigGroup;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnablePennywayRedisDomainConfig(value = {
        PennywayRedisDomainConfigGroup.REDISSON_INFRA
})
public class DomainConfig {
}
