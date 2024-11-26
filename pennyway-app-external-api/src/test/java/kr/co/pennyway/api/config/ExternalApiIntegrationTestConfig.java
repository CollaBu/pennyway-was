package kr.co.pennyway.api.config;

import kr.co.pennyway.PennywayExternalApiApplication;
import kr.co.pennyway.common.PennywayCommonApplication;
import kr.co.pennyway.domain.RedisPackageLocation;
import kr.co.pennyway.domain.domains.JpaPackageLocation;
import kr.co.pennyway.infra.PennywayInfraApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(
        basePackageClasses = {
                PennywayExternalApiApplication.class,
                PennywayInfraApplication.class,
                RedisPackageLocation.class,
                JpaPackageLocation.class,
                PennywayCommonApplication.class
        }
)
public class ExternalApiIntegrationTestConfig {
}