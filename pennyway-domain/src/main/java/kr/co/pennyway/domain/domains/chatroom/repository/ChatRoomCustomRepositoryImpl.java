package kr.co.pennyway.domain.domains.chatroom.repository;

import com.querydsl.core.types.ConstructorExpression;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.co.pennyway.domain.common.util.QueryDslUtil;
import kr.co.pennyway.domain.common.util.SliceUtil;
import kr.co.pennyway.domain.domains.chatroom.domain.QChatRoom;
import kr.co.pennyway.domain.domains.chatroom.dto.ChatRoomDetail;
import kr.co.pennyway.domain.domains.member.domain.QChatMember;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ChatRoomCustomRepositoryImpl implements ChatRoomCustomRepository {
    private final JPAQueryFactory queryFactory;

    private final QChatRoom chatRoom = QChatRoom.chatRoom;
    private final QChatMember chatMember = QChatMember.chatMember;

    @Override
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
    public Slice<ChatRoomDetail> findChatRooms(Long userId, String target, Pageable pageable) {
        // 멤버 수를 계산하는 서브쿼리
        JPAQuery<Integer> memberCountExpr = queryFactory
                .select(chatMember.count().intValue())
                .from(chatMember)
                .where(
                        chatMember.chatRoom.id.eq(chatRoom.id),
                        chatMember.deletedAt.isNull()
                )
                .groupBy(chatMember.chatRoom.id);

        // 사용자가 가입하지 않은 채팅방을 찾는 서브쿼리
        JPQLQuery<Long> eligibleRoomsQuery = queryFactory
                .select(chatMember.chatRoom.id)
                .from(chatMember)
                .where(
                        chatMember.user.id.ne(userId),
                        chatMember.deletedAt.isNull()
                )
                .groupBy(chatMember.chatRoom.id);

        // 채팅방 목록 조회
        List<ChatRoomDetail> results = queryFactory
                .select(createChatRoomDetailConstructorExpression(memberCountExpr))
                .from(chatRoom)
                .where(
                        chatRoom.id.in(eligibleRoomsQuery),
                        QueryDslUtil.matchAgainstTwoElemNaturalMode(
                                chatRoom.title,
                                chatRoom.description,
                                target
                        ),
                        chatRoom.deletedAt.isNull()
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize() + 1)
                .fetch();

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
                countExpr
        );
    }
}
