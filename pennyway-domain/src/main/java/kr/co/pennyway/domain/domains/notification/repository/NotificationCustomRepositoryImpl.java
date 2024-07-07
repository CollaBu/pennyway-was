package kr.co.pennyway.domain.domains.notification.repository;

import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.co.pennyway.domain.domains.notification.domain.QNotification;
import kr.co.pennyway.domain.domains.notification.type.Announcement;
import kr.co.pennyway.domain.domains.notification.type.NoticeType;
import kr.co.pennyway.domain.domains.user.domain.QUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class NotificationCustomRepositoryImpl implements NotificationCustomRepository {
    private final JPAQueryFactory queryFactory;

    private final QUser user = QUser.user;
    private final QNotification notification = QNotification.notification;

    @Override
    public void saveDailySpendingAnnounceInBulk(List<Long> userIds, LocalDateTime publishedAt, Announcement announcement) {
        queryFactory.insert(notification)
                .columns(
                        notification.type,
                        notification.announcement,
                        notification.receiver.id
                )
                .select(
                        JPAExpressions
                                .select(
                                        Expressions.constant(NoticeType.ANNOUNCEMENT),
                                        Expressions.constant(announcement),
                                        user.id
                                )
                                .from(user)
                                .where(user.id.in(userIds)
                                        .and(
                                                JPAExpressions.selectOne()
                                                        .from(notification)
                                                        .where(notification.receiver.id.eq(user.id)
                                                                .and(notification.createdAt.dayOfYear().eq(publishedAt.getDayOfYear()))
                                                                .and(notification.createdAt.year().eq(publishedAt.getYear()))
                                                                .and(notification.type.eq(NoticeType.ANNOUNCEMENT))
                                                                .and(notification.announcement.eq(announcement))
                                                        )
                                                        .notExists()
                                        )
                                )
                )
                .execute();
    }
}
