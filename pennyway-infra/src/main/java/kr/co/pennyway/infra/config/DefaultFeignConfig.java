package kr.co.pennyway.infra.config;

import feign.codec.Encoder;
import feign.form.FormEncoder;
import org.springframework.context.annotation.Bean;

public class DefaultFeignConfig {
    @Bean
    Encoder formEncoder() {
        return new FormEncoder();
    }
}
