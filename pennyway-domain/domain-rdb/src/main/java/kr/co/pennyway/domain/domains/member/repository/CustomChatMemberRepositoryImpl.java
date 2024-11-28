package kr.co.pennyway.domain.domains.member.repository;

import com.querydsl.core.types.ConstantImpl;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.co.pennyway.domain.domains.member.domain.QChatMember;
import kr.co.pennyway.domain.domains.member.dto.ChatMemberResult;
import kr.co.pennyway.domain.domains.member.type.ChatMemberRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

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
                        .and(chatMember.user.id.eq(userId))
                        .and(chatMember.deletedAt.isNull()))
                .fetchFirst() != null;
    }

    @Override
    public boolean existsOwnershipChatRoomByUserId(Long userId) {
        return queryFactory.select(ConstantImpl.create(1))
                .from(chatMember)
                .where(chatMember.user.id.eq(userId)
                        .and(chatMember.role.eq(ChatMemberRole.ADMIN))
                        .and(chatMember.deletedAt.isNull()))
                .fetchFirst() != null;
    }

    @Override
    public Optional<ChatMemberResult.Detail> findAdminByChatRoomId(Long chatRoomId) {
        ChatMemberResult.Detail result =
                queryFactory.select(
                                Projections.constructor(
                                        ChatMemberResult.Detail.class,
                                        chatMember.id,
                                        chatMember.user.name,
                                        chatMember.role,
                                        chatMember.notifyEnabled,
                                        chatMember.user.id,
                                        chatMember.createdAt
                                )
                        )
                        .from(chatMember)
                        .where(chatMember.chatRoom.id.eq(chatRoomId)
                                .and(chatMember.role.eq(ChatMemberRole.ADMIN))
                                .and(chatMember.deletedAt.isNull()))
                        .fetchFirst();

        return Optional.ofNullable(result);
    }
}
