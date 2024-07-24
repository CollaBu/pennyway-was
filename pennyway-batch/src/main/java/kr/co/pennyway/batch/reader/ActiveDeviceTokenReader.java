package kr.co.pennyway.batch.reader;

import com.querydsl.core.types.Projections;
import jakarta.persistence.EntityManagerFactory;
import kr.co.pennyway.batch.common.dto.DeviceTokenOwner;
import kr.co.pennyway.batch.common.reader.QuerydslNoOffsetPagingItemReader;
import kr.co.pennyway.batch.common.reader.expression.Expression;
import kr.co.pennyway.batch.common.reader.options.QuerydslNoOffsetNumberOptions;
import kr.co.pennyway.batch.common.reader.options.QuerydslNoOffsetOptions;
import kr.co.pennyway.domain.domains.device.domain.QDeviceToken;
import kr.co.pennyway.domain.domains.user.domain.QUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ActiveDeviceTokenReader {
    private final EntityManagerFactory emf;

    private final QUser user = QUser.user;
    private final QDeviceToken deviceToken = QDeviceToken.deviceToken;

    @Bean
    @StepScope
    public QuerydslNoOffsetPagingItemReader<DeviceTokenOwner> querydslNoOffsetPagingItemReader() {
        QuerydslNoOffsetOptions<DeviceTokenOwner> options = QuerydslNoOffsetNumberOptions.of(user.id, Expression.ASC, "userId");

        return new QuerydslNoOffsetPagingItemReader<>(emf, 1000, options, queryFactory -> queryFactory
                .select(
                        Projections.constructor(
                                DeviceTokenOwner.class,
                                user.id,
                                user.name,
                                deviceToken.token
                        )
                )
                .from(deviceToken)
                .innerJoin(user).on(deviceToken.user.id.eq(user.id))
                .where(deviceToken.activated.isTrue().and(user.notifySetting.accountBookNotify.isTrue()))
        );
    }
}
