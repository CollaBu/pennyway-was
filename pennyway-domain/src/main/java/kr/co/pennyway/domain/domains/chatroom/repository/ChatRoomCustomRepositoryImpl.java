package kr.co.pennyway.domain.domains.chatroom.repository;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.ConstructorExpression;
import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.*;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.querydsl.jpa.sql.JPASQLQuery;
import com.querydsl.sql.SQLTemplates;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import kr.co.pennyway.domain.common.util.SliceUtil;
import kr.co.pennyway.domain.domains.chatroom.domain.ChatRoom;
import kr.co.pennyway.domain.domains.chatroom.domain.QChatRoom;
import kr.co.pennyway.domain.domains.chatroom.dto.ChatRoomDetail;
import kr.co.pennyway.domain.domains.member.domain.ChatMember;
import kr.co.pennyway.domain.domains.member.domain.QChatMember;
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
    private final JPAQueryFactory queryFactory;
    private final SQLTemplates sqlTemplates;

    @PersistenceContext
    private final EntityManager entityManager;

    private final QChatRoom chatRoom = QChatRoom.chatRoom;
    private final QChatMember chatMember = QChatMember.chatMember;

    @Override
    @Transactional(readOnly = true)
    public List<ChatRoomDetail> findChatRoomsByUserId(Long userId) {
        JPAQuery<Integer> memberCountExpr = queryFactory
                .select(chatMember.count().intValue())
                .from(chatMember)
                .where(
                        chatMember.chatRoom.id.eq(chatRoom.id),
                        chatMember.deletedAt.isNull()
                )
                .groupBy(chatMember.chatRoom.id);

        return queryFactory
                .select(createChatRoomDetailConstructorExpression(memberCountExpr))
                .from(chatRoom)
                .where(
                        chatRoom.id.in(
                                queryFactory.select(chatMember.chatRoom.id)
                                        .from(chatMember)
                                        .where(chatMember.user.id.eq(userId))
                        )
                )
                .fetch();
    }

    @Override
    @Transactional(readOnly = true)
    public Slice<ChatRoomDetail> findChatRooms(Long userId, String target, Pageable pageable) {
        log.info("userId: {}, target: {}, pageable: {}", userId, target, pageable);

        JPASQLQuery<Tuple> jpaSqlQuery = new JPASQLQuery<>(entityManager, sqlTemplates);

        // 별칭 정의
        final StringPath CM = stringPath("cm");
        final StringPath CM_COUNT = stringPath("cm_count");

        final String CHAT_ROOM_ID = "chat_room_id";
        final String MEMBER_COUNT = "member_count";

        // EntityPath 정의
        EntityPath<ChatMember> chatMemberPath = new EntityPathBase<>(ChatMember.class, "chat_member");
        EntityPath<ChatRoom> chatRoomPath = new EntityPathBase<>(ChatRoom.class, "chat_room");

        // 컬럼 경로 정의
        NumberPath<Long> chatRoomIdPath = Expressions.numberPath(Long.class, chatMemberPath, "chat_room_id");
        NumberPath<Long> userIdPath = Expressions.numberPath(Long.class, chatMemberPath, "user_id");
        DateTimePath<LocalDateTime> deletedAtPath = Expressions.dateTimePath(LocalDateTime.class, chatMemberPath, "deleted_at");
        DateTimePath<LocalDateTime> roomDeletedAtPath = Expressions.dateTimePath(LocalDateTime.class, chatRoomPath, "deleted_at");

        // 사용자가 가입하지 않은 채팅방 필터링 서브쿼리
        JPQLQuery<Long> eligibleRoomsQuery = JPAExpressions
                .select(chatRoomIdPath)
                .from(chatMemberPath)
                .where(
                        userIdPath.ne(userId),
                        deletedAtPath.isNull()
                )
                .groupBy(chatRoomIdPath);
        log.info("eligibleRoomsQuery: {}", eligibleRoomsQuery);

        // 멤버 수 계산 서브쿼리
        JPQLQuery<Tuple> memberCountQuery = JPAExpressions
                .select(
                        chatRoomIdPath.as(CHAT_ROOM_ID),
                        Expressions.numberTemplate(Long.class, "count(*)", chatMemberPath).intValue().as(MEMBER_COUNT)
                )
                .from(chatMemberPath)
                .where(deletedAtPath.isNull())
                .groupBy(chatRoomIdPath);
        log.info("memberCountQuery: {}", memberCountQuery);
        log.info("memberCountQuery Type: {}", memberCountQuery.getType());

        // MATCH AGAINST 표현식
        BooleanExpression matchExpr = Expressions.numberTemplate(
                Double.class,
                "MATCH({0}, {1}) AGAINST({2} IN NATURAL LANGUAGE MODE)",
                Expressions.stringPath(chatRoomPath, "title"),
                Expressions.stringPath(chatRoomPath, "description"),
                target
        ).goe(0.0);
        log.info("matchExpr: {}", matchExpr);

        // 메인 쿼리
        List<ChatRoomDetail> results = jpaSqlQuery
                .select(Projections.constructor(
                        ChatRoomDetail.class,
                        Expressions.numberPath(Long.class, chatRoomPath, "id"),
                        Expressions.stringPath(chatRoomPath, "title"),
                        Expressions.stringPath(chatRoomPath, "description"),
                        Expressions.stringPath(chatRoomPath, "background_image_url"),
                        Expressions.numberPath(Integer.class, chatRoomPath, "password"),
                        Expressions.dateTemplate(     // 이 부분만 수정
                                LocalDateTime.class,
                                "DATE_FORMAT({0}, '%Y-%m-%d %H:%i:%s')",
                                "createdAt"
                        ),
                        Expressions.numberPath(Integer.class, CM_COUNT, MEMBER_COUNT)
                ))
                .from(chatRoomPath)
                .innerJoin(eligibleRoomsQuery, CM)
                .on(Expressions.numberPath(Long.class, chatRoomPath, "id")
                        .eq(Expressions.numberPath(Long.class, CM, CHAT_ROOM_ID)))
                .leftJoin(memberCountQuery, CM_COUNT)
                .on(Expressions.numberPath(Long.class, chatRoomPath, "id")
                        .eq(Expressions.numberPath(Long.class, CM_COUNT, CHAT_ROOM_ID)))
                .where(
                        matchExpr,
                        roomDeletedAtPath.isNull()
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize() + 1)
                .fetch();
        log.info("results: {}", results);

        return SliceUtil.toSlice(results, pageable);
    }

    /**
     * {@link ChatRoomDetail} 생성자 표현식을 생성한다.
     *
     * @param countExpr JPAQuery<Integer> 타입의 멤버 수를 계산하는 서브쿼리
     * @return 생성된 ChatRoomDetail 생성자 표현식
     */
    private ConstructorExpression<ChatRoomDetail> createChatRoomDetailConstructorExpression(JPAQuery<Integer> countExpr) {
        return Projections.constructor(
                ChatRoomDetail.class,
                chatRoom.id,
                chatRoom.title,
                chatRoom.description,
                chatRoom.backgroundImageUrl,
                chatRoom.password,
                chatRoom.createdAt,
                new Coalesce<Integer>(countExpr, Expressions.constant(0))
        );
    }
}
