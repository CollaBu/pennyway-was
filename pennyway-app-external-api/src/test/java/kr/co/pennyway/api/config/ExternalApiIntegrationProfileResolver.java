package kr.co.pennyway.api.config;

import org.springframework.lang.NonNull;
import org.springframework.test.context.ActiveProfilesResolver;

public class ExternalApiIntegrationProfileResolver implements ActiveProfilesResolver {
    @Override
    @NonNull
    public String[] resolve(@NonNull Class<?> testClass) {
        return new String[]{"common", "infra", "domain"};
    }
}
