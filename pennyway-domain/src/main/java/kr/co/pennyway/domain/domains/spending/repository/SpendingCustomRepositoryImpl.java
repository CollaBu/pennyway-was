package kr.co.pennyway.domain.domains.spending.repository;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.co.pennyway.domain.common.util.QueryDslUtil;
import kr.co.pennyway.domain.domains.spending.domain.QSpending;
import kr.co.pennyway.domain.domains.spending.domain.QSpendingCustomCategory;
import kr.co.pennyway.domain.domains.spending.domain.Spending;
import kr.co.pennyway.domain.domains.spending.dto.TotalSpendingAmount;
import kr.co.pennyway.domain.domains.user.domain.QUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class SpendingCustomRepositoryImpl implements SpendingCustomRepository {
    private final JPAQueryFactory queryFactory;

    private final QUser user = QUser.user;
    private final QSpending spending = QSpending.spending;
    private final QSpendingCustomCategory spendingCustomCategory = QSpendingCustomCategory.spendingCustomCategory;

    @Override
    public Optional<TotalSpendingAmount> findTotalSpendingAmountByUserId(Long userId, int year, int month) {
        TotalSpendingAmount result = queryFactory.select(
                        Projections.constructor(
                                TotalSpendingAmount.class,
                                spending.spendAt.year(),
                                spending.spendAt.month(),
                                spending.amount.sum()
                        )
                ).from(user)
                .leftJoin(spending).on(user.id.eq(spending.user.id))
                .where(user.id.eq(userId)
                        .and(spending.spendAt.year().eq(year))
                        .and(spending.spendAt.month().eq(month)))
                .groupBy(spending.spendAt.year(), spending.spendAt.month())
                .fetchOne();

        return Optional.ofNullable(result);
    }

    @Override
    public Optional<List<Spending>> findByYearAndMonth(Long userId, int year, int month) {
        Sort sort = Sort.by(Sort.Order.desc("spendAt"));
        List<OrderSpecifier<?>> orderSpecifiers = QueryDslUtil.getOrderSpecifier(sort);

        List<Spending> result = queryFactory.selectFrom(spending)
                .leftJoin(spending.user, user)
                .leftJoin(spending.spendingCustomCategory, spendingCustomCategory).fetchJoin()
                .where(spending.spendAt.year().eq(year)
                        .and(spending.spendAt.month().eq(month)))
                .orderBy(orderSpecifiers.toArray(new OrderSpecifier[0]))
                .fetch();

        return Optional.ofNullable(result);
    }

}
