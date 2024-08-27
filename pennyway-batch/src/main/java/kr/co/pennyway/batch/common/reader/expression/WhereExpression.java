package kr.co.pennyway.batch.common.reader.expression;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.StringPath;

/**
 * 첫페이지 조회시에는 >=, <=
 * 두번째 페이지부터는 >, <
 */
public enum WhereExpression {
    GT(
            (id, page, currentId) -> page == 0 ? id.goe(currentId) : id.gt(currentId),
            (id, page, currentId) -> page == 0 ? id.goe(currentId) : id.gt(currentId)),
    LT(
            (id, page, currentId) -> page == 0 ? id.loe(currentId) : id.lt(currentId),
            (id, page, currentId) -> page == 0 ? id.loe(currentId) : id.lt(currentId)
    );

    private final WhereStringFunction string;
    private final WhereNumberFunction number;

    WhereExpression(WhereStringFunction string, WhereNumberFunction number) {
        this.string = string;
        this.number = number;
    }

    public BooleanExpression expression(StringPath id, int page, String currentId) {
        return this.string.apply(id, page, currentId);
    }

    public <N extends Number & Comparable<?>> BooleanExpression expression(NumberPath<N> id, int page, N currentId) {
        return this.number.apply(id, page, currentId);
    }
}