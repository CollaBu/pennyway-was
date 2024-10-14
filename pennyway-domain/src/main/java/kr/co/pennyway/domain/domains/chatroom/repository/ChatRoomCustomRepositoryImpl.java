package kr.co.pennyway.domain.domains.chatroom.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.co.pennyway.domain.domains.chatroom.domain.QChatRoom;
import kr.co.pennyway.domain.domains.chatroom.dto.ChatRoomDetail;
import kr.co.pennyway.domain.domains.member.domain.QChatMember;
import lombok.RequiredArgsConstructor;
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
        return queryFactory
                .select(
                        Projections.constructor(
                                ChatRoomDetail.class,
                                chatRoom.id,
                                chatRoom.title,
                                chatRoom.description,
                                chatRoom.backgroundImageUrl,
                                chatRoom.password,
                                chatRoom.createdAt,
                                queryFactory.select(chatMember.count().intValue())
                                        .from(chatMember)
                                        .where(chatMember.chatRoom.id.eq(chatRoom.id))
                        )
                )
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
}
