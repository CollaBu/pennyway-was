package kr.co.pennyway.api.apis.chat.service;

import kr.co.pennyway.api.apis.chat.dto.ChatRoomRes;
import kr.co.pennyway.api.config.fixture.ChatMemberFixture;
import kr.co.pennyway.api.config.fixture.ChatRoomFixture;
import kr.co.pennyway.api.config.fixture.UserFixture;
import kr.co.pennyway.domain.context.account.service.UserService;
import kr.co.pennyway.domain.context.chat.service.ChatMemberService;
import kr.co.pennyway.domain.context.chat.service.ChatMessageService;
import kr.co.pennyway.domain.domains.chatroom.domain.ChatRoom;
import kr.co.pennyway.domain.domains.member.domain.ChatMember;
import kr.co.pennyway.domain.domains.member.dto.ChatMemberResult;
import kr.co.pennyway.domain.domains.member.exception.ChatMemberErrorCode;
import kr.co.pennyway.domain.domains.member.exception.ChatMemberErrorException;
import kr.co.pennyway.domain.domains.member.type.ChatMemberRole;
import kr.co.pennyway.domain.domains.message.domain.ChatMessage;
import kr.co.pennyway.domain.domains.message.domain.ChatMessageBuilder;
import kr.co.pennyway.domain.domains.message.type.MessageCategoryType;
import kr.co.pennyway.domain.domains.message.type.MessageContentType;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@Slf4j
@ExtendWith(MockitoExtension.class)
public class ChatRoomWithParticipantsSearchServiceTest {
    private final Long userId = 1L;
    private ChatRoom chatRoom;

    @InjectMocks
    private ChatRoomWithParticipantsSearchService service;
    @Mock
    private UserService userService;
    @Mock
    private ChatMemberService chatMemberService;
    @Mock
    private ChatMessageService chatMessageService;

    @BeforeEach
    void setUp() {
        chatRoom = ChatRoomFixture.PUBLIC_CHAT_ROOM.toEntity(1L);
    }

    @Test
    @DisplayName("관리자인 사용자가 채팅방 참여자 정보와 최근 메시지를 성공적으로 조회한다")
    public void successToRetrieveChatRoomWithParticipantsAndRecentMessages() {
        // given
        ChatMember myInfo = createChatMember(userId, UserFixture.GENERAL_USER.toUser(), chatRoom, ChatMemberRole.ADMIN);
        List<ChatMessage> recentMessages = createRecentMessages();
        List<ChatMemberResult.Detail> recentParticipants = createRecentParticipantDetails();
        List<ChatMemberResult.Summary> otherParticipants = createOtherParticipantSummaries();

        given(userService.readUser(userId)).willReturn(Optional.of(UserFixture.GENERAL_USER.toUser()));
        given(chatMemberService.readChatMember(userId, chatRoom.getId())).willReturn(Optional.of(myInfo));
        given(chatMessageService.readRecentMessages(eq(chatRoom.getId()), anyInt())).willReturn(recentMessages);
        given(chatMemberService.readChatMembersByUserIds(eq(chatRoom.getId()), anySet())).willReturn(recentParticipants);
        given(chatMemberService.readChatMemberIdsByUserIdsNotIn(eq(chatRoom.getId()), anySet())).willReturn(otherParticipants);

        // when
        ChatRoomRes.RoomWithParticipants result = service.execute(userId, chatRoom.getId());

        // then
        log.debug("result: {}", result);

        assertAll(
                () -> assertEquals(userId, result.myInfo().id()),
                () -> assertEquals(2, result.recentParticipants().size()),
                () -> assertEquals(2, result.otherParticipants().size()),
                () -> assertEquals(3, result.recentMessages().size())
        );

        // verify
        verify(userService).readUser(userId);
        verify(chatMemberService).readChatMember(userId, chatRoom.getId());
        verify(chatMessageService).readRecentMessages(eq(chatRoom.getId()), anyInt());
        verify(chatMemberService).readChatMembersByUserIds(eq(chatRoom.getId()), anySet());
        verify(chatMemberService).readChatMemberIdsByUserIdsNotIn(eq(chatRoom.getId()), anySet());
        verify(chatMemberService, never()).readAdmin(chatRoom.getId());
    }

    @Test
    @DisplayName("일반 회원이 채팅방 참여자 정보와 최근 메시지를 조회하면 관리자 정보도 함께 조회된다")
    public void memberSuccessToRetrieveChatRoomWithParticipantsIncludingAdmin() {
        // given
        ChatMember myInfo = createChatMember(userId, UserFixture.GENERAL_USER.toUser(), chatRoom, ChatMemberRole.MEMBER);
        ChatMemberResult.Detail adminDetail = new ChatMemberResult.Detail(2L, "Admin", ChatMemberRole.ADMIN, true, 2L, LocalDateTime.now(), myInfo.getUser().getProfileImageUrl());
        List<ChatMessage> recentMessages = createRecentMessages();
        List<ChatMemberResult.Detail> recentParticipants = createRecentParticipantDetails();
        List<ChatMemberResult.Summary> otherParticipants = createOtherParticipantSummaries();

        given(userService.readUser(userId)).willReturn(Optional.of(UserFixture.GENERAL_USER.toUser()));
        given(chatMemberService.readChatMember(userId, chatRoom.getId())).willReturn(Optional.of(myInfo));
        given(chatMessageService.readRecentMessages(eq(chatRoom.getId()), eq(15))).willReturn(recentMessages);
        given(chatMemberService.readChatMembersByUserIds(eq(chatRoom.getId()), anySet())).willReturn(recentParticipants);
        given(chatMemberService.readAdmin(chatRoom.getId())).willReturn(Optional.of(adminDetail));
        given(chatMemberService.readChatMemberIdsByUserIdsNotIn(eq(chatRoom.getId()), anySet())).willReturn(otherParticipants);

        // when
        ChatRoomRes.RoomWithParticipants result = service.execute(userId, chatRoom.getId());

        // then
        assertAll(
                () -> assertEquals(userId, result.myInfo().id()),
                () -> assertEquals(ChatMemberRole.MEMBER, result.myInfo().role()),
                () -> assertEquals(3, result.recentParticipants().size()),  // 관리자 정보가 포함되어야 함
                () -> assertTrue(result.recentParticipants().stream()
                        .anyMatch(member -> member.role() == ChatMemberRole.ADMIN)),
                () -> assertEquals(2, result.otherParticipants().size()),
                () -> assertEquals(3, result.recentMessages().size())
        );

        // verify
        verify(chatMemberService).readAdmin(chatRoom.getId());
    }

    @Test
    @DisplayName("존재하지 않는 채팅방 멤버 조회 시 예외가 발생한다")
    void throwExceptionWhenChatMemberNotFound() {
        // given
        given(userService.readUser(userId)).willReturn(Optional.of(UserFixture.GENERAL_USER.toUser()));
        given(chatMemberService.readChatMember(userId, chatRoom.getId())).willReturn(Optional.empty());

        // when
        ChatMemberErrorException exception = assertThrows(ChatMemberErrorException.class,
                () -> service.execute(userId, chatRoom.getId()));

        // then
        assertEquals(ChatMemberErrorCode.NOT_FOUND, exception.getBaseErrorCode());

        verify(chatMemberService).readChatMember(userId, chatRoom.getId());
        verifyNoMoreInteractions(chatMemberService, chatMessageService);
    }

    private List<ChatMemberResult.Detail> createRecentParticipantDetails() {
        return List.of(
                new ChatMemberResult.Detail(2L, "User2", ChatMemberRole.MEMBER, true, 20L, LocalDateTime.now(), null),
                new ChatMemberResult.Detail(3L, "User3", ChatMemberRole.MEMBER, true, 30L, LocalDateTime.now(), null)
        );
    }

    private List<ChatMemberResult.Summary> createOtherParticipantSummaries() {
        return List.of(
                new ChatMemberResult.Summary(5L, "User5"),
                new ChatMemberResult.Summary(6L, "User6")
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
