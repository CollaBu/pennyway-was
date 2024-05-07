package kr.co.pennyway.domain.common.repository;

import com.querydsl.core.types.*;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import kr.co.pennyway.domain.common.util.QueryDslUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.querydsl.QSort;
import org.springframework.data.querydsl.SimpleEntityPathResolver;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Map;

public class ExtendedRepositoryImpl<T> implements ExtendedRepository<T> {
    private final EntityManager em;
    private final JPAQueryFactory queryFactory;
    private final EntityPath<T> path;

    public ExtendedRepositoryImpl(JpaEntityInformation<T, ?> entityInformation, EntityManager entityManager) {
        this.em = entityManager;
        this.queryFactory = new JPAQueryFactory(entityManager);
        this.path = SimpleEntityPathResolver.INSTANCE.createPath(entityInformation.getJavaType());
    }

    public ExtendedRepositoryImpl(Class<T> type, EntityManager entityManager) {
        this.em = entityManager;
        this.queryFactory = new JPAQueryFactory(entityManager);
        this.path = new EntityPathBase<>(type, "entity");
    }

    @Override
    public List<T> findList(Predicate predicate, QueryHandler queryHandler, Sort sort) {
        return this.buildWithoutSelect(predicate, null, queryHandler, sort).select(path).fetch();
    }

    @Override
    public Page<T> findPage(Predicate predicate, QueryHandler queryHandler, Pageable pageable) {
        Assert.notNull(pageable, "pageable must not be null!");

        JPAQuery<?> query = this.buildWithoutSelect(predicate, null, queryHandler, pageable.getSort()).select(path);

        int totalSize = query.fetch().size();
        query = query.offset(pageable.getOffset()).limit(pageable.getPageSize());

        return new PageImpl<>(query.select(path).fetch(), pageable, totalSize);
    }

    @Override
    public <P> List<P> selectList(Predicate predicate, Class<P> type, Map<String, Expression<?>> bindings, QueryHandler queryHandler, Sort sort) {
        return this.buildWithoutSelect(predicate, bindings, queryHandler, sort).select(Projections.bean(type, bindings)).fetch();
    }

    @Override
    public <P> Page<P> selectPage(Predicate predicate, Class<P> type, Map<String, Expression<?>> bindings, QueryHandler queryHandler, Pageable pageable) {
        Assert.notNull(pageable, "pageable must not be null!");

        JPAQuery<?> query = this.buildWithoutSelect(predicate, bindings, queryHandler, pageable.getSort()).select(path);

        int totalSize = query.fetch().size();
        query = query.offset(pageable.getOffset()).limit(pageable.getPageSize());

        return new PageImpl<>(query.select(Projections.bean(type, bindings)).fetch(), pageable, totalSize);
    }

    /**
     * 파라미터를 기반으로 Querydsl의 JPAQuery를 생성하는 메서드
     */
    private JPAQuery<?> buildWithoutSelect(Predicate predicate, Map<String, Expression<?>> bindings, QueryHandler queryHandler, Sort sort) {
        JPAQuery<?> query = queryFactory.from(path);

        applyPredicate(predicate, query);
        applyQueryHandler(queryHandler, query);
        applySort(query, sort, bindings);

        return query;
    }

    /**
     * Querydsl의 JPAQuery에 Predicate를 적용하는 메서드 <br/>
     * Predicate가 null이 아닐 경우에만 적용
     */
    private void applyPredicate(Predicate predicate, JPAQuery<?> query) {
        if (predicate != null) query.where(predicate);
    }

    /**
     * Querydsl의 JPAQuery에 QueryHandler를 적용하는 메서드 <br/>
     * QueryHandler가 null이 아닐 경우에만 적용
     */
    private void applyQueryHandler(QueryHandler queryHandler, JPAQuery<?> query) {
        if (queryHandler != null) queryHandler.apply(query);
    }

    /**
     * Querydsl의 JPAQuery에 Sort를 적용하는 메서드 <br/>
     * Sort가 null이 아닐 경우에만 적용 <br/>
     * Sort가 QSort일 경우에는 OrderSpecifier를 적용하고, 그 외의 경우에는 OrderSpecifier를 생성하여 적용
     */
    private void applySort(JPAQuery<?> query, Sort sort, Map<String, Expression<?>> bindings) {
        if (sort != null) {
            if (sort instanceof QSort qSort) {
                query.orderBy(qSort.getOrderSpecifiers().toArray(new OrderSpecifier[0]));
            } else {
                applySortOrders(query, sort, bindings);
            }
        }
    }

    private void applySortOrders(JPAQuery<?> query, Sort sort, Map<String, Expression<?>> bindings) {
        for (Sort.Order order : sort) {
            OrderSpecifier.NullHandling queryDslNullHandling = QueryDslUtil.getQueryDslNullHandling(order);

            OrderSpecifier<?> os = QueryDslUtil.getOrderSpecifier(order, bindings, queryDslNullHandling);

            query.orderBy(os);
        }
    }
}