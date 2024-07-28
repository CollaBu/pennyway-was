package kr.co.pennyway.batch.common.reader.expression;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.StringPath;

public enum Expression {
    ASC(WhereExpression.GT, OrderExpression.ASC),
    DESC(WhereExpression.LT, OrderExpression.DESC);

    private final WhereExpression where;
    private final OrderExpression order;

    Expression(WhereExpression where, OrderExpression order) {
        this.where = where;
        this.order = order;
    }

    public boolean isAsc() {
        return this == ASC;
    }

    public BooleanExpression where(StringPath id, int page, String currentId) {
        return where.expression(id, page, currentId);
    }

    public <N extends Number & Comparable<?>> BooleanExpression where(NumberPath<N> id, int page, N currentId) {
        return where.expression(id, page, currentId);
    }

    public OrderSpecifier<String> order(StringPath id) {
        return isAsc() ? id.asc() : id.desc();
    }

    public <N extends Number & Comparable<?>> OrderSpecifier<N> order(NumberPath<N> id) {
        return isAsc() ? id.asc() : id.desc();
    }
}