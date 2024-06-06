package kr.co.pennyway.domain.domains.target.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.co.pennyway.domain.domains.target.domain.QTargetAmount;
import kr.co.pennyway.domain.domains.target.domain.TargetAmount;
import kr.co.pennyway.domain.domains.user.domain.QUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class TargetAmountCustomRepositoryImpl implements TargetAmountCustomRepository {
    private final JPAQueryFactory queryFactory;

    private final QUser user = QUser.user;
    private final QTargetAmount targetAmount = QTargetAmount.targetAmount;

    /**
     * 사용자의 가장 최근 목표 금액을 조회한다.
     *
     * @return 최근 목표 금액이 존재하지 않을 경우 Optional.empty()를 반환하며, 당월 목표 금액 정보일 수도 있다.
     */
    @Override
    public Optional<TargetAmount> findRecentOneByUserId(Long userId) {
        TargetAmount result = queryFactory.selectFrom(targetAmount)
                .innerJoin(user).on(targetAmount.user.id.eq(user.id))
                .where(user.id.eq(userId)
                        .and(targetAmount.amount.gt(-1)))
                .orderBy(targetAmount.createdAt.desc())
                .fetchFirst();

        return Optional.ofNullable(result);
    }

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
