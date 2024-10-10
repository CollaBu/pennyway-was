package kr.co.pennyway.infra.config;

import kr.co.pennyway.infra.client.guid.IdGenerator;
import kr.co.pennyway.infra.client.guid.TsidGenerator;
import kr.co.pennyway.infra.common.importer.PennywayInfraConfig;
import org.springframework.context.annotation.Bean;

public class GuidConfig implements PennywayInfraConfig {
    @Bean
    public IdGenerator<Long> idGenerator() {
        return new TsidGenerator();
    }
}
