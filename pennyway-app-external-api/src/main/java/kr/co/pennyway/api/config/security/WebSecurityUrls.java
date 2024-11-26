package kr.co.pennyway.api.config.security;

public final class WebSecurityUrls {
    public static final String[] READ_ONLY_PUBLIC_ENDPOINTS = {"/favicon.ico", "/v1/duplicate/**"};
    public static final String[] PUBLIC_ENDPOINTS = {"/v1/questions/**"};
    public static final String[] ANONYMOUS_ENDPOINTS = {"/v1/auth/**", "/v1/phone/**", "/v1/find/**"};
    public static final String[] AUTHENTICATED_ENDPOINTS = {"/v1/auth"};
    public static final String[] SWAGGER_ENDPOINTS = {"/api-docs/**", "/v3/api-docs/**", "/swagger-ui/**", "/swagger",};
}
