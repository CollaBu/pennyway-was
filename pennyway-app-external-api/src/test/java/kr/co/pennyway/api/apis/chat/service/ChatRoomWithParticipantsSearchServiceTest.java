package kr.co.pennyway.api.apis.chat.service;

import kr.co.pennyway.api.apis.chat.dto.ChatRoomRes;
import kr.co.pennyway.api.config.fixture.ChatMemberFixture;
import kr.co.pennyway.api.config.fixture.ChatRoomFixture;
import kr.co.pennyway.api.config.fixture.UserFixture;
import kr.co.pennyway.domain.common.redis.message.domain.ChatMessage;
import kr.co.pennyway.domain.common.redis.message.domain.ChatMessageBuilder;
import kr.co.pennyway.domain.common.redis.message.service.ChatMessageService;
import kr.co.pennyway.domain.common.redis.message.type.MessageCategoryType;
import kr.co.pennyway.domain.common.redis.message.type.MessageContentType;
import kr.co.pennyway.domain.domains.chatroom.domain.ChatRoom;
import kr.co.pennyway.domain.domains.member.domain.ChatMember;
import kr.co.pennyway.domain.domains.member.exception.ChatMemberErrorCode;
import kr.co.pennyway.domain.domains.member.exception.ChatMemberErrorException;
import kr.co.pennyway.domain.domains.member.service.ChatMemberService;
import kr.co.pennyway.domain.domains.member.type.ChatMemberRole;
import kr.co.pennyway.domain.domains.user.domain.User;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@Slf4j
@ExtendWith(MockitoExtension.class)
public class ChatRoomWithParticipantsSearchServiceTest {
    private final Long userId = 1L;
    private ChatRoom chatRoom;

    @InjectMocks
    private ChatRoomWithParticipantsSearchService service;
    @Mock
    private ChatMemberService chatMemberService;
    @Mock
    private ChatMessageService chatMessageService;

    @BeforeEach
    void setUp() {
        chatRoom = ChatRoomFixture.PUBLIC_CHAT_ROOM.toEntity(1L);
    }

    @Test
    @DisplayName("채팅방 참여자 정보와 최근 메시지를 성공적으로 조회한다")
    public void successToRetrieveChatRoomWithParticipantsAndRecentMessages() {
        // given
        ChatMember myInfo = createChatMember(userId, UserFixture.GENERAL_USER.toUser(), chatRoom, ChatMemberRole.ADMIN);
        List<ChatMessage> recentMessages = createRecentMessages();
        List<ChatMember> recentParticipants = createRecentParticipants();
        List<Long> otherMemberIds = List.of(5L, 6L);

        given(chatMemberService.readChatMember(userId, chatRoom.getId())).willReturn(Optional.of(myInfo));
        given(chatMessageService.readRecentMessages(eq(chatRoom.getId()), anyInt())).willReturn(recentMessages);
        given(chatMemberService.readChatMembersByMemberIdIn(eq(chatRoom.getId()), anySet())).willReturn(recentParticipants);
        given(chatMemberService.readChatMemberIdsByMemberIdNotIn(eq(chatRoom.getId()), anySet())).willReturn(otherMemberIds);

        // when
        ChatRoomRes.RoomWithParticipants result = service.execute(userId, chatRoom.getId());

        // then
        log.debug("result: {}", result);

        assertAll(
                () -> assertEquals(userId, result.myInfo().id()),
                () -> assertEquals(2, result.recentParticipants().size()),
                () -> assertEquals(2, result.otherParticipantIds().size()),
                () -> assertEquals(3, result.recentMessages().size())
        );

        // verify
        verify(chatMemberService).readChatMember(userId, chatRoom.getId());
        verify(chatMessageService).readRecentMessages(eq(chatRoom.getId()), anyInt());
        verify(chatMemberService).readChatMembersByMemberIdIn(eq(chatRoom.getId()), anySet());
        verify(chatMemberService).readChatMemberIdsByMemberIdNotIn(eq(chatRoom.getId()), anySet());
    }

    @Test
    @DisplayName("존재하지 않는 채팅방 멤버 조회 시 예외가 발생한다")
    void throwExceptionWhenChatMemberNotFound() {
        // given
        given(chatMemberService.readChatMember(userId, chatRoom.getId())).willReturn(Optional.empty());

        // when
        ChatMemberErrorException exception = assertThrows(ChatMemberErrorException.class,
                () -> service.execute(userId, chatRoom.getId()));

        // then
        assertEquals(ChatMemberErrorCode.NOT_FOUND, exception.getBaseErrorCode());

        verify(chatMemberService).readChatMember(userId, chatRoom.getId());
        verifyNoMoreInteractions(chatMemberService, chatMessageService);
    }

    private List<ChatMember> createRecentParticipants() {
        return List.of(
                createChatMember(2L, UserFixture.GENERAL_USER.toUser(), chatRoom, ChatMemberRole.MEMBER),
                createChatMember(3L, UserFixture.GENERAL_USER.toUser(), chatRoom, ChatMemberRole.MEMBER)
        );
    }

    private ChatMember createChatMember(Long userId, User user, ChatRoom chatRoom, ChatMemberRole role) {
        ChatMember member;

        switch (role) {
            case ADMIN -> member = ChatMemberFixture.ADMIN.toEntity(user, chatRoom);
            case MEMBER -> member = ChatMemberFixture.MEMBER.toEntity(user, chatRoom);
            default -> throw new IllegalArgumentException("Unexpected role: " + role);
        }

        ReflectionTestUtils.setField(member, "id", userId);
        return member;
    }

    private List<ChatMessage> createRecentMessages() {
        return List.of(
                createChatMessage(3L, "Message 3", 1L),
                createChatMessage(2L, "Message 2", 2L),
                createChatMessage(1L, "Message 1", 3L)
        );
    }

    private ChatMessage createChatMessage(Long chatId, String content, Long senderId) {
        return ChatMessageBuilder.builder()
                .chatRoomId(chatRoom.getId())
                .chatId(chatId)
                .content(content)
                .contentType(MessageContentType.TEXT)
                .categoryType(MessageCategoryType.NORMAL)
                .sender(senderId)
                .build();
    }
}
