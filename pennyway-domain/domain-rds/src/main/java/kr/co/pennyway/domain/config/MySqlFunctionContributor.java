package kr.co.pennyway.domain.config;

import org.hibernate.boot.model.FunctionContributions;
import org.hibernate.boot.model.FunctionContributor;
import org.hibernate.query.sqm.function.SqmFunctionRegistry;
import org.hibernate.type.StandardBasicTypes;
import org.hibernate.type.spi.TypeConfiguration;

public class MySqlFunctionContributor implements FunctionContributor {
    public static final String TWO_COLUMN_NATURAL_FUNCTION_NAME = "two_column_natural";
    private static final String TWO_COLUMN_NATURAL_PATTERN = "match(?1, ?2) against(?3 in natural language mode)";

    @Override
    public void contributeFunctions(final FunctionContributions functionContributions) {
        SqmFunctionRegistry registry = functionContributions.getFunctionRegistry();
        TypeConfiguration typeConfiguration = functionContributions.getTypeConfiguration();

        registry.registerPattern(TWO_COLUMN_NATURAL_FUNCTION_NAME, TWO_COLUMN_NATURAL_PATTERN, typeConfiguration.getBasicTypeRegistry().resolve(StandardBasicTypes.DOUBLE));
    }
}
