package kr.co.pennyway.domain.domains.member.service;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Predicate;
import kr.co.pennyway.common.annotation.DomainService;
import kr.co.pennyway.domain.common.repository.QueryHandler;
import kr.co.pennyway.domain.domains.chatroom.domain.ChatRoom;
import kr.co.pennyway.domain.domains.member.domain.ChatMember;
import kr.co.pennyway.domain.domains.member.domain.QChatMember;
import kr.co.pennyway.domain.domains.member.dto.ChatMemberResult;
import kr.co.pennyway.domain.domains.member.exception.ChatMemberErrorCode;
import kr.co.pennyway.domain.domains.member.exception.ChatMemberErrorException;
import kr.co.pennyway.domain.domains.member.repository.ChatMemberRepository;
import kr.co.pennyway.domain.domains.member.type.ChatMemberRole;
import kr.co.pennyway.domain.domains.user.domain.QUser;
import kr.co.pennyway.domain.domains.user.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@DomainService
@RequiredArgsConstructor
public class ChatMemberRdbService {
    private final ChatMemberRepository chatMemberRepository;

    private final QUser qUser = QUser.user;
    private final QChatMember qChatMember = QChatMember.chatMember;

    @Transactional
    public ChatMember create(ChatMember chatMember) {
        return chatMemberRepository.save(chatMember);
    }

    @Transactional
    public ChatMember createAdmin(User user, ChatRoom chatRoom) {
        ChatMember chatMember = ChatMember.of(user, chatRoom, ChatMemberRole.ADMIN);

        return chatMemberRepository.save(chatMember);
    }

    @Transactional
    public ChatMember createMember(User user, ChatRoom chatRoom) {
        Set<ChatMember> chatMembers = chatMemberRepository.findByChatRoom_IdAndUserId(chatRoom.getId(), user.getId());

        if (chatMembers.stream().anyMatch(ChatMember::isActive)) {
            log.warn("사용자는 이미 채팅방에 가입되어 있습니다. chatRoomId: {}, userId: {}", chatRoom.getId(), user.getId());
            throw new ChatMemberErrorException(ChatMemberErrorCode.ALREADY_JOINED);
        }

        if (chatMembers.stream().anyMatch(ChatMember::isBanned)) {
            log.warn("사용자는 채팅방에서 추방된 이력이 존재합니다. chatRoomId: {}, userId: {}", chatRoom.getId(), user.getId());
            throw new ChatMemberErrorException(ChatMemberErrorCode.BANNED);
        }

        ChatMember chatMember = ChatMember.of(user, chatRoom, ChatMemberRole.MEMBER);

        return chatMemberRepository.save(chatMember);
    }

    @Transactional(readOnly = true)
    public Optional<ChatMember> readChatMemberByChatMemberId(Long chatMemberId) {
        return chatMemberRepository.findByChatMember_Id(chatMemberId);
    }

    @Transactional(readOnly = true)
    public Optional<ChatMember> readChatMember(Long userId, Long chatRoomId) {
        return chatMemberRepository.findActiveChatMember(chatRoomId, userId).stream().findFirst();
    }

    @Transactional(readOnly = true)
    public Optional<ChatMemberResult.Detail> readAdmin(Long chatRoomId) {
        return chatMemberRepository.findAdminByChatRoomId(chatRoomId);
    }

    @Transactional(readOnly = true)
    public Set<ChatMember> readChatMembersByChatRoomId(Long chatRoomId) {
        return chatMemberRepository.findByChatRoom_Id(chatRoomId);
    }

    @Transactional(readOnly = true)
    public List<ChatMemberResult.Detail> readChatMembersByIdIn(Long chatRoomId, Set<Long> chatMemberIds) {
        QueryHandler queryHandler = query -> query.innerJoin(qUser).on(qChatMember.userId.eq(qUser.id));

        Predicate predicate = qChatMember.chatRoom.id.eq(chatRoomId)
                .and(qChatMember.id.in(chatMemberIds))
                .and(qChatMember.deletedAt.isNull());

        Map<String, Expression<?>> bindings = new LinkedHashMap<>();
        bindings.put("id", qChatMember.id);
        bindings.put("name", qUser.name);
        bindings.put("role", qChatMember.role);
        bindings.put("notification", qChatMember.notifyEnabled);
        bindings.put("userId", qUser.id);
        bindings.put("createdAt", qChatMember.createdAt);
        bindings.put("profileImageUrl", qUser.profileImageUrl);

        return chatMemberRepository.selectList(predicate, ChatMemberResult.Detail.class, bindings, queryHandler, null);
    }

    @Transactional(readOnly = true)
    public List<ChatMemberResult.Detail> readChatMembersByUserIdIn(Long chatRoomId, Set<Long> userIds) {
        QueryHandler queryHandler = query -> query.innerJoin(qUser).on(qChatMember.userId.eq(qUser.id));

        Predicate predicate = qChatMember.chatRoom.id.eq(chatRoomId)
                .and(qUser.id.in(userIds))
                .and(qChatMember.deletedAt.isNull());

        Map<String, Expression<?>> bindings = new LinkedHashMap<>();
        bindings.put("id", qUser.id);
        bindings.put("name", qUser.name);
        bindings.put("role", qChatMember.role);
        bindings.put("notification", qChatMember.notifyEnabled);
        bindings.put("userId", qUser.id);
        bindings.put("createdAt", qChatMember.createdAt);
        bindings.put("profileImageUrl", qUser.profileImageUrl);

        return chatMemberRepository.selectList(predicate, ChatMemberResult.Detail.class, bindings, queryHandler, null);
    }

    @Transactional(readOnly = true)
    public List<ChatMemberResult.Summary> readChatMemberIdsByUserIdNotIn(Long chatRoomId, Set<Long> userIds) {
        QueryHandler queryHandler = query -> query.innerJoin(qUser).on(qChatMember.userId.eq(qUser.id));

        Predicate predicate = qChatMember.chatRoom.id.eq(chatRoomId)
                .and(qUser.id.notIn(userIds))
                .and(qChatMember.deletedAt.isNull());

        Map<String, Expression<?>> bindings = new LinkedHashMap<>();
        bindings.put("id", qChatMember.id);
        bindings.put("name", qUser.name);

        return chatMemberRepository.selectList(predicate, ChatMemberResult.Summary.class, bindings, queryHandler, null);
    }

    @Transactional(readOnly = true)
    public Set<Long> readChatRoomIdsByUserId(Long userId) {
        return chatMemberRepository.findChatRoomIdsByUserId(userId);
    }

    @Transactional(readOnly = true)
    public Set<Long> readUserIdsByChatRoomId(Long chatRoomId) {
        return chatMemberRepository.findUserIdsByChatRoomId(chatRoomId);
    }

    /**
     * 채팅방에 해당 유저가 존재하는지 확인한다.
     * 이 때, 삭제된 사용자 데이터는 조회하지 않는다.
     */
    @Transactional(readOnly = true)
    public boolean isExists(Long chatRoomId, Long userId) {
        return chatMemberRepository.existsByChatRoomIdAndUserId(chatRoomId, userId);
    }

    /**
     * 삭제된 사용자 데이터는 조회하지 않는다.
     */
    @Transactional(readOnly = true)
    public boolean isExists(Long chatRoomId, Long userId, Long chatMemberId) {
        return chatMemberRepository.existsByChatRoomIdAndUserIdAndId(chatRoomId, userId, chatMemberId);
    }

    @Transactional(readOnly = true)
    public boolean hasUserChatRoomOwnership(Long userId) {
        return chatMemberRepository.existsOwnershipChatRoomByUserId(userId);
    }

    @Transactional(readOnly = true)
    public long countActiveMembers(Long chatRoomId) {
        return chatMemberRepository.countByChatRoomIdAndActive(chatRoomId);
    }

    @Transactional
    public void update(ChatMember chatMember) {
        chatMemberRepository.save(chatMember);
    }

    @Transactional
    public void deleteAllByChatRoomId(Long chatRoomId) {
        chatMemberRepository.deleteAllByChatRoomId(chatRoomId);
    }
}
