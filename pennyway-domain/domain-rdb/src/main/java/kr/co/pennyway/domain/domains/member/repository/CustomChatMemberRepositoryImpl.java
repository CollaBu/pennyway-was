package kr.co.pennyway.domain.domains.member.repository;

import com.querydsl.core.types.ConstantImpl;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.co.pennyway.domain.domains.member.domain.QChatMember;
import kr.co.pennyway.domain.domains.member.dto.ChatMemberResult;
import kr.co.pennyway.domain.domains.member.type.ChatMemberRole;
import kr.co.pennyway.domain.domains.user.domain.QUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CustomChatMemberRepositoryImpl implements CustomChatMemberRepository {
    private final JPAQueryFactory queryFactory;

    private final QChatMember chatMember = QChatMember.chatMember;
    private final QUser user = QUser.user;

    @Override
    public boolean existsByChatRoomIdAndUserId(Long chatRoomId, Long userId) {
        return queryFactory.select(ConstantImpl.create(1))
                .from(chatMember)
                .where(chatMember.chatRoom.id.eq(chatRoomId)
                        .and(chatMember.userId.eq(userId))
                        .and(chatMember.deletedAt.isNull()))
                .fetchFirst() != null;
    }

    @Override
    public boolean existsOwnershipChatRoomByUserId(Long userId) {
        return queryFactory.select(ConstantImpl.create(1))
                .from(chatMember)
                .where(chatMember.userId.eq(userId)
                        .and(chatMember.role.eq(ChatMemberRole.ADMIN))
                        .and(chatMember.deletedAt.isNull()))
                .fetchFirst() != null;
    }

    @Override
    public boolean existsByChatRoomIdAndUserIdAndId(Long chatRoomId, Long userId, Long chatMemberId) {
        return queryFactory.select(ConstantImpl.create(1))
                .from(chatMember)
                .where(chatMember.chatRoom.id.eq(chatRoomId)
                        .and(chatMember.userId.eq(userId))
                        .and(chatMember.id.eq(chatMemberId))
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
                                        user.name,
                                        chatMember.role,
                                        chatMember.notifyEnabled,
                                        user.id,
                                        chatMember.createdAt,
                                        user.profileImageUrl
                                )
                        )
                        .from(chatMember)
                        .innerJoin(user).on(chatMember.userId.eq(user.id))
                        .where(chatMember.chatRoom.id.eq(chatRoomId)
                                .and(chatMember.role.eq(ChatMemberRole.ADMIN))
                                .and(chatMember.deletedAt.isNull()))
                        .fetchFirst();

        return Optional.ofNullable(result);
    }
}
