package kr.co.pennyway.api.config;

import kr.co.pennyway.infra.common.importer.EnablePennywayInfraConfig;
import kr.co.pennyway.infra.common.importer.PennywayInfraConfigGroup;
import kr.co.pennyway.infra.common.properties.AppleOidcProperties;
import kr.co.pennyway.infra.common.properties.GoogleOidcProperties;
import kr.co.pennyway.infra.common.properties.KakaoOidcProperties;
import kr.co.pennyway.infra.common.properties.ServerProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({
        ServerProperties.class,
        AppleOidcProperties.class,
        GoogleOidcProperties.class,
        KakaoOidcProperties.class
})
@EnablePennywayInfraConfig({
        PennywayInfraConfigGroup.FCM,
        PennywayInfraConfigGroup.DISTRIBUTED_COORDINATION_CONFIG
})
public class InfraConfig {
}
