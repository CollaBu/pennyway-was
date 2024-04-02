package kr.co.pennyway.infra.common.importer;

import kr.co.pennyway.common.util.MapUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.DeferredImportSelector;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.lang.NonNull;

import java.util.Arrays;
import java.util.Map;

public class PennywayInfraConfigImportSelector implements DeferredImportSelector {
    @NotNull
    @Override
    public String[] selectImports(@NonNull AnnotationMetadata metadata) {
        return Arrays.stream(getGroups(metadata))
                .map(v -> v.getConfigClass().getName())
                .toArray(String[]::new);
    }

    private PennywayInfraConfigGroup[] getGroups(AnnotationMetadata metadata) {
        Map<String, Object> attributes = metadata.getAnnotationAttributes(EnablePennywayInfraConfig.class.getName());
        return (PennywayInfraConfigGroup[]) MapUtils.getObject(attributes, "value", new PennywayInfraConfigGroup[]{});
    }
}
