package kr.co.pennyway.batch.common.reader.expression;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.StringPath;

@FunctionalInterface
public interface WhereStringFunction {
    BooleanExpression apply(StringPath id, int page, String currentId);
}