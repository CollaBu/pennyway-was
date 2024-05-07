package kr.co.pennyway.domain.common.util;

import com.querydsl.core.types.*;
import com.querydsl.core.types.dsl.Expressions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

    /**
     * OrderSpecifier를 생성할 때, Sort.Order의 정보를 이용하여 OrderSpecifier.NullHandling을 적용하는 메서드
     *
     * @param order : {@link Sort.Order}
     * @return {@link OrderSpecifier.NullHandling}
     */
    public static OrderSpecifier.NullHandling getQueryDslNullHandling(Sort.Order order) {
        Function<Sort.NullHandling, OrderSpecifier.NullHandling> castToQueryDsl = nullHandling -> switch (nullHandling) {
            case NATIVE -> OrderSpecifier.NullHandling.Default;
            case NULLS_FIRST -> OrderSpecifier.NullHandling.NullsFirst;
            case NULLS_LAST -> OrderSpecifier.NullHandling.NullsLast;
        };

        return castToQueryDsl.apply(order.getNullHandling());
    }

    /**
     * OrderSpecifier를 생성할 때, Sort.Order의 정보를 이용하여 OrderSpecifier.NullHandling을 적용하는 메서드
     *
     * @param order        : {@link Sort.Order}
     * @param nullHandling : {@link OrderSpecifier.NullHandling}
     * @return {@link OrderSpecifier}
     */
    public static OrderSpecifier<?> getOrderSpecifier(Sort.Order order, OrderSpecifier.NullHandling nullHandling) {
        Order orderBy = order.isAscending() ? Order.ASC : Order.DESC;

        return createOrderSpecifier(orderBy, Expressions.stringPath(order.getProperty()), nullHandling);
    }

    /**
     * Expression이 Operation이고 Operator가 ALIAS일 경우, OrderSpecifier를 생성할 때, Expression을 StringPath로 변환하여 생성한다. <br/>
     * 그 외의 경우에는 OrderSpecifier를 생성한다.
     *
     * @param order                : {@link Sort.Order}
     * @param bindings             : 검색 조건에 해당하는 도메인(혹은 DTO)의 필드 정보. {@code binding}은 Map<String, Expression<?>> 형태로 전달된다.
     * @param queryDslNullHandling : {@link OrderSpecifier.NullHandling}
     * @return {@link OrderSpecifier}
     */
    public static OrderSpecifier<?> getOrderSpecifier(Sort.Order order, Map<String, Expression<?>> bindings, OrderSpecifier.NullHandling queryDslNullHandling) {
        Order orderBy = order.isAscending() ? Order.ASC : Order.DESC;

        if (bindings != null && bindings.containsKey(order.getProperty())) {
            Expression<?> expression = bindings.get(order.getProperty());
            return createOrderSpecifier(orderBy, expression, queryDslNullHandling);
        } else {
            return createOrderSpecifier(orderBy, Expressions.stringPath(order.getProperty()), queryDslNullHandling);
        }
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
