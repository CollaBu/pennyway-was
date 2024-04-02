package kr.co.pennyway.infra.config;

import feign.codec.Encoder;
import feign.form.FormEncoder;
import feign.okhttp.OkHttpClient;
import org.springframework.context.annotation.Bean;

public class DefaultFeignConfig {
    @Bean
    public OkHttpClient client() {
        return new OkHttpClient();
    }

    @Bean
    Encoder formEncoder() {
        return new FormEncoder();
    }
}
