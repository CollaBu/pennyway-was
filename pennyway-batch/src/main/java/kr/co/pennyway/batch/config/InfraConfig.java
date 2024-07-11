package kr.co.pennyway.batch.config;

import kr.co.pennyway.infra.common.importer.EnablePennywayInfraConfig;
import kr.co.pennyway.infra.common.importer.PennywayInfraConfigGroup;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnablePennywayInfraConfig({
        PennywayInfraConfigGroup.FCM
})
public class InfraConfig {
}
