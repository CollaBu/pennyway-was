package kr.co.pennyway.domain.config;

import kr.co.pennyway.common.PennywayCommonApplication;
import kr.co.pennyway.domain.DomainPackageLocation;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(
        basePackageClasses = {
                DomainPackageLocation.class,
                PennywayCommonApplication.class
        }
)
public class DomainIntegrationTestConfig {
}
