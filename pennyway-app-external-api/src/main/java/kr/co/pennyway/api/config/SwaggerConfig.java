package kr.co.pennyway.api.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import kr.co.pennyway.api.common.swagger.ApiExceptionExplainParser;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.util.ObjectUtils;
import org.springframework.web.filter.ForwardedHeaderFilter;
import org.springframework.web.method.HandlerMethod;

@Configuration
@OpenAPIDefinition(
        servers = {
                @Server(url = "${pennyway.server.domain.local}", description = "Local Server"),
                @Server(url = "${pennyway.server.domain.dev}", description = "Develop Server")
        }
)
@RequiredArgsConstructor
public class SwaggerConfig {
    private static final String JWT = "JWT";
    private final Environment environment;

    @Bean
    public OpenAPI openAPI() {
        String activeProfile = "";
        if (!ObjectUtils.isEmpty(environment.getActiveProfiles()) && environment.getActiveProfiles().length >= 1) {
            activeProfile = environment.getActiveProfiles()[0];
        }

        SecurityRequirement securityRequirement = new SecurityRequirement().addList(JWT);

        return new OpenAPI()
                .info(apiInfo(activeProfile))
                .addSecurityItem(securityRequirement)
                .components(securitySchemes());
    }

    @Bean
    public GroupedOpenApi allApi() {
        String[] targets = {"kr.co.pennyway.api.apis"};

        return GroupedOpenApi.builder()
                .packagesToScan(targets)
                .group("전체 보기")
                .addOperationCustomizer(customizer())
                .build();
    }

    @Bean
    public GroupedOpenApi authApi() {
        String[] targets = {"kr.co.pennyway.api.apis.auth"};

        return GroupedOpenApi.builder()
                .packagesToScan(targets)
                .group("사용자 인증")
                .addOperationCustomizer(customizer())
                .build();
    }

    @Bean
    public GroupedOpenApi userApi() {
        String[] targets = {"kr.co.pennyway.api.apis.users", "kr.co.pennyway.api.apis.notification"};

        return GroupedOpenApi.builder()
                .packagesToScan(targets)
                .group("사용자 기본 기능")
                .addOperationCustomizer(customizer())
                .build();
    }

    @Bean
    public GroupedOpenApi storageApi() {
        String[] targets = {"kr.co.pennyway.api.apis.storage"};

        return GroupedOpenApi.builder()
                .packagesToScan(targets)
                .group("정적 파일 저장")
                .addOperationCustomizer(customizer())
                .build();
    }

    @Bean
    public GroupedOpenApi ledgerApi() {
        String[] targets = {"kr.co.pennyway.api.apis.ledger"};

        return GroupedOpenApi.builder()
                .packagesToScan(targets)
                .group("지출 관리")
                .addOperationCustomizer(customizer())
                .build();
    }

    @Bean
    public GroupedOpenApi socketApi() {
        String[] targets = {"kr.co.pennyway.api.apis.socket"};

        return GroupedOpenApi.builder()
                .packagesToScan(targets)
                .group("서비스 탐색 서비스")
                .addOperationCustomizer(customizer())
                .build();
    }

    @Bean
    public GroupedOpenApi chatApi() {
        String[] targets = {"kr.co.pennyway.api.apis.chat"};

        return GroupedOpenApi.builder()
                .packagesToScan(targets)
                .group("채팅")
                .addOperationCustomizer(customizer())
                .build();
    }

    @Bean
    public GroupedOpenApi backOfficeApi() {
        String[] targets = {"kr.co.pennyway.api.apis.question"};

        return GroupedOpenApi.builder()
                .packagesToScan(targets)
                .group("백오피스")
                .addOperationCustomizer(customizer())
                .build();
    }

    @Bean
    ForwardedHeaderFilter forwardedHeaderFilter() {
        return new ForwardedHeaderFilter();
    }

    @Bean
    public OperationCustomizer customizer() {
        return (Operation operation, HandlerMethod handlerMethod) -> {
            ApiExceptionExplainParser.parse(operation, handlerMethod);
            return operation;
        };
    }

    private Components securitySchemes() {
        final var securitySchemeAccessToken = new SecurityScheme()
                .name(JWT)
                .type(SecurityScheme.Type.HTTP)
                .scheme("Bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER)
                .name("Authorization");

        return new Components()
                .addSecuritySchemes(JWT, securitySchemeAccessToken);
    }

    private Info apiInfo(String activeProfile) {
        return new Info()
                .title("Pennyway API (" + activeProfile + ")")
                .description("지출 관리 SNS 플랫폼 Pennyway API 명세서")
                .version("v1.0.0");
    }
}
