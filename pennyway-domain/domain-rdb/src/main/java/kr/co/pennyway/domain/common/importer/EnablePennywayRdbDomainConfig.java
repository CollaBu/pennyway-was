package kr.co.pennyway.domain.common.importer;

import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(PennywayRdbDomainConfigImportSelector.class)
public @interface EnablePennywayRdbDomainConfig {
    PennywayRdbDomainConfigGroup[] value();
}
