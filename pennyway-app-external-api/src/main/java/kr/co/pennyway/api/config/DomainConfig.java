package kr.co.pennyway.api.config;

import kr.co.pennyway.domain.common.importer.EnablePennywayDomainConfig;
import kr.co.pennyway.domain.common.importer.PennywayDomainConfigGroup;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnablePennywayDomainConfig(value = {
        PennywayDomainConfigGroup.REDIS_DOMAIN,
        PennywayDomainConfigGroup.REDISSON_DOMAIN,
        PennywayDomainConfigGroup.LETTUCE_DOMAIN
})
public class DomainConfig {
}
