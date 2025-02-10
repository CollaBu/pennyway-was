package kr.co.pennyway.api.config.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.co.pennyway.api.common.security.filter.JwtAuthenticationFilter;
import kr.co.pennyway.api.common.security.filter.JwtExceptionFilter;
import kr.co.pennyway.domain.context.account.service.ForbiddenTokenService;
import kr.co.pennyway.infra.common.jwt.JwtProvider;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetailsService;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Configuration
public class SecurityFilterConfig {
    private final UserDetailsService userDetailServiceImpl;
    private final ForbiddenTokenService forbiddenTokenService;

    private final JwtProvider accessTokenProvider;

    private final ObjectMapper objectMapper;

    @Bean
    public JwtExceptionFilter jwtExceptionFilter() {
        return new JwtExceptionFilter(objectMapper);
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthorizationFilter() {
        return new JwtAuthenticationFilter(userDetailServiceImpl, forbiddenTokenService, accessTokenProvider);
    }
}
