package kr.co.pennyway.domain.common.importer;

import kr.co.pennyway.common.util.MapUtils;
import org.springframework.context.annotation.DeferredImportSelector;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.lang.NonNull;

import java.util.Arrays;
import java.util.Map;

public class PennywayRdsDomainConfigImportSelector implements DeferredImportSelector {
    @NonNull
    @Override
    public String[] selectImports(@NonNull AnnotationMetadata metadata) {
        return Arrays.stream(getGroups(metadata))
                .map(v -> v.getConfigClass().getName())
                .toArray(String[]::new);
    }

    private PennywayRdsDomainConfigGroup[] getGroups(AnnotationMetadata metadata) {
        Map<String, Object> attributes = metadata.getAnnotationAttributes(EnablePennywayRdsDomainConfig.class.getName());
        return (PennywayRdsDomainConfigGroup[]) MapUtils.getObject(attributes, "value", new PennywayRdsDomainConfigGroup[]{});
    }
}
