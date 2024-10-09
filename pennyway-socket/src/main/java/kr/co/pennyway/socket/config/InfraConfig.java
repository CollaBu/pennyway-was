package kr.co.pennyway.socket.config;

import kr.co.pennyway.infra.common.importer.EnablePennywayInfraConfig;
import kr.co.pennyway.infra.common.importer.PennywayInfraConfigGroup;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnablePennywayInfraConfig({
        PennywayInfraConfigGroup.MESSAGE_BROKER_CONFIG
})
public class InfraConfig {
}
