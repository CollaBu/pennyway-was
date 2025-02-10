package kr.co.pennyway.domain.config;

import org.springframework.lang.NonNull;
import org.springframework.test.context.ActiveProfilesResolver;

public class DomainIntegrationProfileResolver implements ActiveProfilesResolver {
    @Override
    @NonNull
    public String[] resolve(@NonNull Class<?> testClass) {
        return new String[]{"common", "domain"};
    }
}
