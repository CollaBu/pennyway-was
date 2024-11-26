package kr.co.pennyway.domain.common.importer;

import kr.co.pennyway.common.util.MapUtils;
import org.springframework.context.annotation.DeferredImportSelector;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.lang.NonNull;

import java.util.Arrays;
import java.util.Map;

public class PennywayRdbDomainConfigImportSelector implements DeferredImportSelector {
    @NonNull
    @Override
    public String[] selectImports(@NonNull AnnotationMetadata metadata) {
        return Arrays.stream(getGroups(metadata))
                .map(v -> v.getConfigClass().getName())
                .toArray(String[]::new);
    }

    private PennywayRdbDomainConfigGroup[] getGroups(AnnotationMetadata metadata) {
        Map<String, Object> attributes = metadata.getAnnotationAttributes(EnablePennywayRdbDomainConfig.class.getName());
        return (PennywayRdbDomainConfigGroup[]) MapUtils.getObject(attributes, "value", new PennywayRdbDomainConfigGroup[]{});
    }
}
