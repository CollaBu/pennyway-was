package kr.co.pennyway.api.config;

import kr.co.pennyway.api.common.converter.ProviderConverter;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addFormatters(FormatterRegistry registrar) {
        registrar.addConverter(new ProviderConverter());
    }
}
