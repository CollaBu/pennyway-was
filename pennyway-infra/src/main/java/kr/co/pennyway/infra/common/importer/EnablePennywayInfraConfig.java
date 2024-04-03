package kr.co.pennyway.infra.common.importer;

import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(PennywayInfraConfigImportSelector.class)
public @interface EnablePennywayInfraConfig {
    PennywayInfraConfigGroup[] value();
}
