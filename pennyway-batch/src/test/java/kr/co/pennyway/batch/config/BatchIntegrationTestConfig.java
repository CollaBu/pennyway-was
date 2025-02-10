package kr.co.pennyway.batch.config;

import kr.co.pennyway.PennywayBatchApplication;
import kr.co.pennyway.common.PennywayCommonApplication;
import kr.co.pennyway.domain.RedisPackageLocation;
import kr.co.pennyway.domain.domains.JpaPackageLocation;
import kr.co.pennyway.infra.PennywayInfraApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(
        basePackageClasses = {
                PennywayBatchApplication.class,
                PennywayInfraApplication.class,
                JpaPackageLocation.class,
                RedisPackageLocation.class,
                PennywayCommonApplication.class
        }
)
public class BatchIntegrationTestConfig {
}