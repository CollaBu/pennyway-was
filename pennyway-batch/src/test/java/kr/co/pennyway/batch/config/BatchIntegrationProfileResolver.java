package kr.co.pennyway.batch.config;

import org.springframework.lang.NonNull;
import org.springframework.test.context.ActiveProfilesResolver;

public class BatchIntegrationProfileResolver implements ActiveProfilesResolver {
    @Override
    @NonNull
    public String[] resolve(@NonNull Class<?> testClass) {
        return new String[]{"common", "infra", "domain"};
    }
}
