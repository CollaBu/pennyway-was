package kr.co.pennyway.domain.domains.device.repository;

import com.querydsl.core.group.GroupBy;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.co.pennyway.domain.domains.device.domain.QDeviceToken;
import kr.co.pennyway.domain.domains.device.dto.DeviceTokenOwner;
import kr.co.pennyway.domain.domains.user.domain.QUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class DeviceTokenCustomRepositoryImpl implements DeviceTokenCustomRepository {
    private final JPAQueryFactory queryFactory;

    private final QUser user = QUser.user;
    private final QDeviceToken deviceToken = QDeviceToken.deviceToken;
    
    @Override
    public Page<DeviceTokenOwner> findActivatedDeviceTokenOwners(Pageable pageable) {
        List<DeviceTokenOwner> content = queryFactory
                .select(
                        Projections.constructor(
                                DeviceTokenOwner.class,
                                user.id,
                                user.name,
                                GroupBy.list(deviceToken.token)
                        )
                )
                .leftJoin(user).on(deviceToken.user.id.eq(user.id))
                .from(deviceToken)
                .where(deviceToken.activated.isTrue().and(user.notifySetting.accountBookNotify.isTrue()))
                .groupBy(user.id)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(user.id.asc())
                .fetch();

        JPAQuery<Long> count = queryFactory
                .select(deviceToken.count())
                .where(deviceToken.activated.isTrue());

        return PageableExecutionUtils.getPage(content, pageable, () -> count.fetch().size());
    }
}
