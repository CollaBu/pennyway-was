package kr.co.pennyway.domain.domains.target.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.co.pennyway.domain.domains.target.domain.QTargetAmount;
import kr.co.pennyway.domain.domains.user.domain.QUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
@RequiredArgsConstructor
public class TargetAmountCustomRepositoryImpl implements TargetAmountCustomRepository {
    private final JPAQueryFactory queryFactory;

    private final QUser user = QUser.user;
    private final QTargetAmount targetAmount = QTargetAmount.targetAmount;

    @Override
    public boolean existsByUserIdThatMonth(Long userId, LocalDate date) {
        return queryFactory.selectOne().from(targetAmount)
                .innerJoin(user).on(targetAmount.user.id.eq(user.id))
                .where(user.id.eq(userId)
                        .and(targetAmount.createdAt.year().eq(date.getYear()))
                        .and(targetAmount.createdAt.month().eq(date.getMonthValue())))
                .fetchFirst() != null;
    }
}
