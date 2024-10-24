package kr.co.pennyway.domain.domains.chatroom.repository;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.*;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.sql.JPASQLQuery;
import com.querydsl.sql.SQLTemplates;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import kr.co.pennyway.domain.common.util.SliceUtil;
import kr.co.pennyway.domain.domains.chatroom.domain.ChatRoom;
import kr.co.pennyway.domain.domains.chatroom.dto.ChatRoomDetail;
import kr.co.pennyway.domain.domains.member.domain.ChatMember;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static com.querydsl.core.types.dsl.Expressions.stringPath;

@Slf4j
@Repository
@RequiredArgsConstructor
public class ChatRoomCustomRepositoryImpl implements ChatRoomCustomRepository {
    private final SQLTemplates sqlTemplates;
    @PersistenceContext
    private final EntityManager entityManager;

    @Override
    @Transactional(readOnly = true)
    public List<ChatRoomDetail> findChatRoomsByUserId(Long userId) {
        JPASQLQuery<Tuple> jpaSqlQuery = new JPASQLQuery<>(entityManager, sqlTemplates);

        // 별칭 정의
        final StringPath MY_ROOMS = stringPath("my_rooms");
        final StringPath ROOM_STATS = stringPath("room_stats");
        final String CHAT_ROOM_ID = "chat_room_id";
        final String MEMBER_COUNT = "member_count";
        final String IS_ADMIN = "is_admin";

        // EntityPath 정의
        EntityPath<ChatMember> Chat_MEMBER_ENTITY_PATH = new EntityPathBase<>(ChatMember.class, "chat_member");
        EntityPath<ChatRoom> CHAT_ROOM_ENTITY_PATH = new EntityPathBase<>(ChatRoom.class, "chat_room");

        // 사용자가 가입한 방 필터링 서브쿼리
        JPQLQuery<Long> myRoomsQuery = JPAExpressions
                .select(Expressions.numberPath(Long.class, Chat_MEMBER_ENTITY_PATH, CHAT_ROOM_ID))
                .from(Chat_MEMBER_ENTITY_PATH)
                .where(
                        Expressions.numberPath(Long.class, Chat_MEMBER_ENTITY_PATH, "user_id").eq(userId),
                        Expressions.dateTimePath(LocalDateTime.class, Chat_MEMBER_ENTITY_PATH, "deleted_at").isNull()
                );

        // 멤버 수와 어드민 여부를 계산하는 서브쿼리
        JPQLQuery<Tuple> roomStatsQuery = JPAExpressions
                .select(
                        Expressions.numberPath(Long.class, Chat_MEMBER_ENTITY_PATH, CHAT_ROOM_ID),
                        Expressions.numberTemplate(Long.class, "COUNT(*)", Chat_MEMBER_ENTITY_PATH).as(MEMBER_COUNT),
                        Expressions.booleanTemplate(
                                "MAX(CASE WHEN user_id = {0} AND role = '0' THEN true ELSE false END)",
                                userId
                        ).as(IS_ADMIN)
                )
                .from(Chat_MEMBER_ENTITY_PATH)
                .where(Expressions.dateTimePath(LocalDateTime.class, Chat_MEMBER_ENTITY_PATH, "deleted_at").isNull())
                .groupBy(Expressions.numberPath(Long.class, Chat_MEMBER_ENTITY_PATH, CHAT_ROOM_ID));

        // 메인 쿼리
        return jpaSqlQuery
                .select(Projections.constructor(
                        ChatRoomDetail.class,
                        Expressions.numberPath(Long.class, CHAT_ROOM_ENTITY_PATH, "id"),
                        Expressions.stringPath(CHAT_ROOM_ENTITY_PATH, "title"),
                        Expressions.stringPath(CHAT_ROOM_ENTITY_PATH, "description"),
                        Expressions.stringPath(CHAT_ROOM_ENTITY_PATH, "background_image_url"),
                        Expressions.numberPath(Integer.class, CHAT_ROOM_ENTITY_PATH, "password"),
                        Expressions.dateTemplate(
                                LocalDateTime.class,
                                "DATE_FORMAT({0}, '%Y-%m-%d %H:%i:%s')",
                                "createdAt"
                        ),
                        Expressions.booleanPath(ROOM_STATS, IS_ADMIN),
                        Expressions.numberPath(Integer.class, ROOM_STATS, MEMBER_COUNT)
                ))
                .from(CHAT_ROOM_ENTITY_PATH)
                .innerJoin(myRoomsQuery, MY_ROOMS)
                .on(Expressions.numberPath(Long.class, CHAT_ROOM_ENTITY_PATH, "id")
                        .eq(Expressions.numberPath(Long.class, MY_ROOMS, CHAT_ROOM_ID)))
                .leftJoin(roomStatsQuery, ROOM_STATS)
                .on(Expressions.numberPath(Long.class, CHAT_ROOM_ENTITY_PATH, "id")
                        .eq(Expressions.numberPath(Long.class, ROOM_STATS, CHAT_ROOM_ID)))
                .where(Expressions.dateTimePath(LocalDateTime.class, CHAT_ROOM_ENTITY_PATH, "deleted_at").isNull())
                .fetch();
    }

    @Override
    @Transactional(readOnly = true)
    public Slice<ChatRoomDetail> findChatRooms(Long userId, String target, Pageable pageable) {
        JPASQLQuery<Tuple> jpaSqlQuery = new JPASQLQuery<>(entityManager, sqlTemplates);

        // 별칭 정의
        final StringPath CM = stringPath("cm");
        final StringPath CM_COUNT = stringPath("cm_count");

        final String CHAT_ROOM_ID = "chat_room_id";
        final String MEMBER_COUNT = "member_count";

        // EntityPath 정의
        EntityPath<ChatMember> CHAT_MEMBER_ENTITY_PATH = new EntityPathBase<>(ChatMember.class, "chat_member");
        EntityPath<ChatRoom> CHAT_ROOM_ENTITY_PATH = new EntityPathBase<>(ChatRoom.class, "chat_room");

        // 컬럼 경로 정의
        NumberPath<Long> CHAT_ROOM_ID_PATH = Expressions.numberPath(Long.class, CHAT_MEMBER_ENTITY_PATH, "chat_room_id");
        NumberPath<Long> USER_ID_PATH = Expressions.numberPath(Long.class, CHAT_MEMBER_ENTITY_PATH, "user_id");
        DateTimePath<LocalDateTime> MEMBER_DELETED_AT_PATH = Expressions.dateTimePath(LocalDateTime.class, CHAT_MEMBER_ENTITY_PATH, "deleted_at");
        DateTimePath<LocalDateTime> ROOM_DELETED_AT_PATH = Expressions.dateTimePath(LocalDateTime.class, CHAT_ROOM_ENTITY_PATH, "deleted_at");

        // 사용자가 가입하지 않은 채팅방 필터링 서브쿼리
        JPQLQuery<Long> eligibleRoomsQuery = JPAExpressions
                .select(CHAT_ROOM_ID_PATH)
                .from(CHAT_MEMBER_ENTITY_PATH)
                .where(
                        MEMBER_DELETED_AT_PATH.isNull(),
                        CHAT_ROOM_ID_PATH.notIn(
                                JPAExpressions
                                        .select(CHAT_ROOM_ID_PATH)
                                        .from(CHAT_MEMBER_ENTITY_PATH)
                                        .where(
                                                USER_ID_PATH.eq(userId),
                                                MEMBER_DELETED_AT_PATH.isNull()
                                        )
                        )
                );

        // 멤버 수 계산 서브쿼리
        JPQLQuery<Tuple> memberCountQuery = JPAExpressions
                .select(
                        CHAT_ROOM_ID_PATH.as(CHAT_ROOM_ID),
                        Expressions.numberTemplate(Long.class, "count(*)", CHAT_MEMBER_ENTITY_PATH).intValue().as(MEMBER_COUNT)
                )
                .from(CHAT_MEMBER_ENTITY_PATH)
                .where(MEMBER_DELETED_AT_PATH.isNull())
                .groupBy(CHAT_ROOM_ID_PATH);

        // MATCH AGAINST 표현식
        BooleanExpression matchExpr = Expressions.booleanTemplate(
                "MATCH({0}, {1}) AGAINST({2} IN NATURAL LANGUAGE MODE)",
                Expressions.stringPath(CHAT_ROOM_ENTITY_PATH, "title"),
                Expressions.stringPath(CHAT_ROOM_ENTITY_PATH, "description"),
                target
        );

        // 메인 쿼리
        List<ChatRoomDetail> results = jpaSqlQuery
                .select(Projections.constructor(
                        ChatRoomDetail.class,
                        Expressions.numberPath(Long.class, CHAT_ROOM_ENTITY_PATH, "id"),
                        Expressions.stringPath(CHAT_ROOM_ENTITY_PATH, "title"),
                        Expressions.stringPath(CHAT_ROOM_ENTITY_PATH, "description"),
                        Expressions.stringPath(CHAT_ROOM_ENTITY_PATH, "background_image_url"),
                        Expressions.numberPath(Integer.class, CHAT_ROOM_ENTITY_PATH, "password"),
                        Expressions.dateTemplate(
                                LocalDateTime.class,
                                "DATE_FORMAT({0}, '%Y-%m-%d %H:%i:%s')",
                                "createdAt"
                        ),
                        Expressions.constant(false),
                        Expressions.numberPath(Integer.class, CM_COUNT, MEMBER_COUNT)
                ))
                .from(CHAT_ROOM_ENTITY_PATH)
                .innerJoin(eligibleRoomsQuery, CM)
                .on(Expressions.numberPath(Long.class, CHAT_ROOM_ENTITY_PATH, "id")
                        .eq(Expressions.numberPath(Long.class, CM, CHAT_ROOM_ID)))
                .leftJoin(memberCountQuery, CM_COUNT)
                .on(Expressions.numberPath(Long.class, CHAT_ROOM_ENTITY_PATH, "id")
                        .eq(Expressions.numberPath(Long.class, CM_COUNT, CHAT_ROOM_ID)))
                .where(
                        matchExpr,
                        ROOM_DELETED_AT_PATH.isNull()
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize() + 1)
                .fetch();

        return SliceUtil.toSlice(results, pageable);
    }

}
