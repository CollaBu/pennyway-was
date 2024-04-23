package kr.co.pennyway.api.config.security;

public class WebSecurityUrls {
    protected static final String[] READ_ONLY_PUBLIC_ENDPOINTS = {"/favicon.ico", "/v1/duplicate/**"};
    protected static final String[] PUBLIC_ENDPOINTS = {"/v1/questions/**"};
    protected static final String[] ANONYMOUS_ENDPOINTS = {"/v1/auth/**", "/v1/phone/**"};
    protected static final String[] SWAGGER_ENDPOINTS = {"/api-docs/**", "/v3/api-docs/**", "/swagger-ui/**", "/swagger",};
}
