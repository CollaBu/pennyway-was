package kr.co.pennyway.batch.common.reader.expression;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.NumberPath;

@FunctionalInterface
public interface WhereNumberFunction<N extends Number & Comparable<?>> {
    BooleanExpression apply(NumberPath<N> id, int page, N currentId);

}