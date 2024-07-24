package kr.co.pennyway.batch.common.reader.options;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.jpa.impl.JPAQuery;
import jakarta.annotation.Nonnull;
import kr.co.pennyway.batch.common.reader.expression.Expression;

public class QuerydslNoOffsetNumberOptions<T, N extends Number & Comparable<?>> extends QuerydslNoOffsetOptions<T> {

    private final NumberPath<N> field;
    private N currentId;
    private N lastId;

    private QuerydslNoOffsetNumberOptions(@Nonnull NumberPath<N> field,
                                          @Nonnull Expression expression) {
        super(field, expression);
        this.field = field;
    }

    private QuerydslNoOffsetNumberOptions(@Nonnull NumberPath<N> field,
                                          @Nonnull Expression expression,
                                          String idName) {
        super(idName, expression);
        this.field = field;
    }

    /**
     * QEntity의 NumberPath 필드를 사용하여 offset을 설정하는 옵션을 생성합니다.
     *
     * @param field      {@link NumberPath} : offset으로 사용할 필드
     * @param expression {@link Expression} : 정렬 방향
     */
    public static <T, N extends Number & Comparable<?>> QuerydslNoOffsetNumberOptions<T, N> of(@Nonnull NumberPath<N> field, @Nonnull Expression expression) {
        return new QuerydslNoOffsetNumberOptions<>(field, expression);
    }

    /**
     * QEintity의 NumberPath 필드를 사용하여 offset을 설정하는 옵션을 생성합니다.
     * <p>
     * 만약, 쿼리의 응답을 QEntity가 아닌 Dto를 사용한 경우 마지막으로 조회한 offset의 값이 저장된 필드를 idName으로 지정해야 하며, Number 타입이어야 합니다.
     *
     * @param field      {@link NumberPath} : offset으로 사용할 필드
     * @param expression {@link Expression} : 정렬 방향
     * @param idName     {@link String} : 마지막으로 조회한 offset이 저장된 필드 이름
     */
    public static <T, N extends Number & Comparable<?>> QuerydslNoOffsetNumberOptions<T, N> of(@Nonnull NumberPath<N> field, @Nonnull Expression expression, String idName) {
        return new QuerydslNoOffsetNumberOptions<>(field, expression, idName);
    }

    public N getCurrentId() {
        return currentId;
    }

    public N getLastId() {
        return lastId;
    }

    @Override
    public void initKeys(JPAQuery<T> query, int page) {
        if (page == 0) {
            initFirstId(query);
            initLastId(query);

            if (logger.isDebugEnabled()) {
                logger.debug("First Key= " + currentId + ", Last Key= " + lastId);
            }
        }
    }

    @Override
    protected void initFirstId(JPAQuery<T> query) {
        JPAQuery<T> clone = query.clone();
        boolean isGroupByQuery = isGroupByQuery(clone);

        if (isGroupByQuery) {
            currentId = clone
                    .select(field)
                    .orderBy(expression.isAsc() ? field.asc() : field.desc())
                    .fetchFirst();
        } else {
            currentId = clone
                    .select(expression.isAsc() ? field.min() : field.max())
                    .fetchFirst();
        }

    }

    @Override
    protected void initLastId(JPAQuery<T> query) {
        JPAQuery<T> clone = query.clone();
        boolean isGroupByQuery = isGroupByQuery(clone);

        if (isGroupByQuery) {
            lastId = clone
                    .select(field)
                    .orderBy(expression.isAsc() ? field.desc() : field.asc())
                    .fetchFirst();
        } else {
            lastId = clone
                    .select(expression.isAsc() ? field.max() : field.min())
                    .fetchFirst();
        }
    }

    @Override
    public JPAQuery<T> createQuery(JPAQuery<T> query, int page) {
        if (currentId == null) {
            return query;
        }

        return query
                .where(whereExpression(page))
                .orderBy(orderExpression());
    }

    private BooleanExpression whereExpression(int page) {
        return expression.where(field, page, currentId)
                .and(expression.isAsc() ? field.loe(lastId) : field.goe(lastId));
    }

    private OrderSpecifier<N> orderExpression() {
        return expression.order(field);
    }

    @Override
    public void resetCurrentId(T item) {
        //noinspection unchecked
        currentId = (N) getFiledValue(item);

        if (logger.isDebugEnabled()) {
            logger.debug("Current Select Key= " + currentId);
        }
    }
}