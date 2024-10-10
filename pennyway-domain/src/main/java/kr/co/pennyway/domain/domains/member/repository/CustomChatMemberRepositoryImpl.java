package kr.co.pennyway.domain.domains.member.repository;

import com.querydsl.core.types.ConstantImpl;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.co.pennyway.domain.domains.member.domain.QChatMember;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CustomChatMemberRepositoryImpl implements CustomChatMemberRepository {
    private final JPAQueryFactory queryFactory;

    private final QChatMember chatMember = QChatMember.chatMember;

    @Override
    public boolean existsByChatRoomIdAndUserId(Long chatRoomId, Long userId) {
        return queryFactory.select(ConstantImpl.create(1))
                .from(chatMember)
                .where(chatMember.chatRoom.id.eq(chatRoomId)
                        .and(chatMember.user.id.eq(userId)))
                .fetchFirst() != null;
    }
}
