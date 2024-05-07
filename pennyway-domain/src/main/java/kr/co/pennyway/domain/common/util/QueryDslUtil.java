package kr.co.pennyway.domain.common.util;

import com.querydsl.core.types.*;
import com.querydsl.core.types.dsl.Expressions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@Slf4j
public class QueryDslUtil {
    private static final Function<Sort.NullHandling, OrderSpecifier.NullHandling> castToQueryDsl = nullHandling -> switch (nullHandling) {
        case NATIVE -> OrderSpecifier.NullHandling.Default;
        case NULLS_FIRST -> OrderSpecifier.NullHandling.NullsFirst;
        case NULLS_LAST -> OrderSpecifier.NullHandling.NullsLast;
    };

    /**
     * Pageable의 sort를 QueryDsl의 OrderSpecifier로 변환하는 메서드
     *
     * @param sort : {@link Sort}
     */
    public static List<OrderSpecifier<?>> getOrderSpecifier(Sort sort) {
        List<OrderSpecifier<?>> orders = new ArrayList<>();

        for (Sort.Order order : sort) {
            OrderSpecifier.NullHandling nullHandling = castToQueryDsl.apply(order.getNullHandling());
            orders.add(getOrderSpecifier(order, nullHandling));
        }

        return orders;
    }

    private static OrderSpecifier<?> getOrderSpecifier(Sort.Order order, OrderSpecifier.NullHandling nullHandling) {
        Order orderBy = order.isAscending() ? Order.ASC : Order.DESC;

        return createOrderSpecifier(orderBy, Expressions.stringPath(order.getProperty()), nullHandling);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static OrderSpecifier<?> createOrderSpecifier(Order orderBy, Expression<?> expression, OrderSpecifier.NullHandling queryDslNullHandling) {
        if (expression instanceof Operation && ((Operation<?>) expression).getOperator() == Ops.ALIAS) {
            return new OrderSpecifier<>(orderBy, Expressions.stringPath(((Operation<?>) expression).getArg(1).toString()), queryDslNullHandling);
        } else {
            return new OrderSpecifier(orderBy, expression, queryDslNullHandling);
        }
    }
}
