package kr.co.pennyway.api.config;

import kr.co.pennyway.api.common.converter.NotifyTypeConverter;
import kr.co.pennyway.api.common.converter.ProviderConverter;
import kr.co.pennyway.api.common.converter.VerificationTypeConverter;
import kr.co.pennyway.domain.common.redis.sign.SignEventLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {
    private final SignEventLogService signEventLogService;

    @Override
    public void addFormatters(FormatterRegistry registrar) {

        registrar.addConverter(new ProviderConverter());
        registrar.addConverter(new VerificationTypeConverter());
        registrar.addConverter(new NotifyTypeConverter());
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SignEventLogInterceptor(signEventLogService))
                .addPathPatterns("/v1/auth/sign-in", "/v1/auth/oauth/sign-up", "/v1/auth/refresh");
    }
}
