package kr.co.pennyway.domain.domains.spending.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.co.pennyway.domain.domains.spending.domain.QSpending;
import kr.co.pennyway.domain.domains.spending.dto.TotalSpendingAmount;
import kr.co.pennyway.domain.domains.user.domain.QUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class SpendingCustomRepositoryImpl implements SpendingCustomRepository {
    private final JPAQueryFactory queryFactory;

    private final QUser user = QUser.user;
    private final QSpending spending = QSpending.spending;

    @Override
    public TotalSpendingAmount findTotalSpendingAmountByUserId(Long userId, int year, int month) {
        return queryFactory.select(
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
                .fetchOne();
    }
}
