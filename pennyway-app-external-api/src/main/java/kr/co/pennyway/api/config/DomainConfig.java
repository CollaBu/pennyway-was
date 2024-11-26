package kr.co.pennyway.api.config;

import org.springframework.context.annotation.Configuration;

@Configuration
@EnablePennywayDomainConfig({
        PennywayDomainConfigGroup.REDISSON
})
public class DomainConfig {
}
