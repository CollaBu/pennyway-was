package kr.co.pennyway.infra.config;

import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;
import org.springframework.cloud.openfeign.clientconfig.HttpClient5FeignConfiguration;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFeignClients(basePackages = "kr.co.pennyway.infra")
@ImportAutoConfiguration({FeignAutoConfiguration.class, HttpClient5FeignConfiguration.class})
public class FeignConfig {
}
