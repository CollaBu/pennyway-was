package kr.co.pennyway.api.config.security;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.security.ConditionalOnDefaultWebSecurity;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.AbstractRequestMatcherRegistry;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
@ConditionalOnDefaultWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private static final String[] READ_ONLY_PUBLIC_ENDPOINTS = {"/favicon.ico", "/v1/duplicate/**"};
    private static final String[] PUBLIC_ENDPOINTS = {"/v1/questions/**"};
    private static final String[] ANONYMOUS_ENDPOINTS = {"/v1/auth/**", "/v1/phone/**"};
    private static final String[] SWAGGER_ENDPOINTS = {"/api-docs/**", "/v3/api-docs/**", "/swagger-ui/**", "/swagger",};


    private final SecurityAdapterConfig securityAdapterConfig;
    private final CorsConfigurationSource corsConfigurationSource;
    private final AccessDeniedHandler accessDeniedHandler;
    private final AuthenticationEntryPoint authenticationEntryPoint;

    @Bean
    @Profile({"local", "dev", "test"})
    @Order(SecurityProperties.BASIC_AUTH_ORDER)
    public SecurityFilterChain filterChainDev(HttpSecurity http) throws Exception {
        return defaultSecurity(http)
                .cors((cors) -> cors.configurationSource(corsConfigurationSource))
                .authorizeHttpRequests(
                        auth -> defaultAuthorizeHttpRequests(auth)
                                .requestMatchers(SWAGGER_ENDPOINTS).permitAll()
                                .anyRequest().authenticated()
                ).build();
    }

    @Bean
    @Profile({"prod"})
    @Order(SecurityProperties.BASIC_AUTH_ORDER)
    public SecurityFilterChain filterChainProd(HttpSecurity http) throws Exception {
        return defaultSecurity(http)
                .cors(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(
                        auth -> defaultAuthorizeHttpRequests(auth).anyRequest().authenticated()
                ).build();
    }

    private HttpSecurity defaultSecurity(HttpSecurity http) throws Exception {
        return http.httpBasic(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement((sessionManagement) ->
                        sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .with(securityAdapterConfig, Customizer.withDefaults())
                .exceptionHandling(
                        exception -> exception
                                .accessDeniedHandler(accessDeniedHandler)
                                .authenticationEntryPoint(authenticationEntryPoint)
                );
    }

    private AbstractRequestMatcherRegistry<AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizedUrl> defaultAuthorizeHttpRequests(
            AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry auth) {
        return auth.requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
                .requestMatchers(HttpMethod.OPTIONS, "*").permitAll()
                .requestMatchers(HttpMethod.GET, READ_ONLY_PUBLIC_ENDPOINTS).permitAll()
                .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                .requestMatchers(ANONYMOUS_ENDPOINTS).anonymous();
    }
}
