package kr.co.pennyway.api.apis.chat.service;

import kr.co.pennyway.api.config.fixture.ChatMemberFixture;
import kr.co.pennyway.api.config.fixture.ChatRoomFixture;
import kr.co.pennyway.api.config.fixture.UserFixture;
import kr.co.pennyway.domain.domains.chatroom.domain.ChatRoom;
import kr.co.pennyway.domain.domains.chatroom.exception.ChatRoomErrorCode;
import kr.co.pennyway.domain.domains.chatroom.exception.ChatRoomErrorException;
import kr.co.pennyway.domain.domains.chatroom.service.ChatRoomService;
import kr.co.pennyway.domain.domains.member.domain.ChatMember;
import kr.co.pennyway.domain.domains.member.service.ChatMemberService;
import kr.co.pennyway.domain.domains.user.domain.User;
import kr.co.pennyway.domain.domains.user.exception.UserErrorException;
import kr.co.pennyway.domain.domains.user.service.UserService;
import kr.co.pennyway.infra.common.event.ChatRoomJoinEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@Slf4j
@ExtendWith(MockitoExtension.class)
public class ChatMemberJoinServiceTest {
    private static final long FULL_ROOM_CAPACITY = 300L;
    private static final long AVAILABLE_CAPACITY = 299L;
    private ChatMemberJoinService chatMemberJoinService;
    @Mock
    private UserService userService;
    @Mock
    private ChatRoomService chatRoomService;
    @Mock
    private ChatMemberService chatMemberService;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    private Long userId = 1L;
    private Long chatRoomId = 1L;

    @BeforeEach
    void setUp() {
        chatMemberJoinService = new ChatMemberJoinService(userService, chatRoomService, chatMemberService, eventPublisher);
    }

    @Test
    @DisplayName("채팅방이 가득 찼을 때 (정원 300명) 가입에 실패한다.")
    void failWhenChatRoomIsFull() {
        // given
        given(chatRoomService.readChatRoom(chatRoomId)).willReturn(Optional.of(createPublicRoom()));
        given(chatMemberService.countActiveMembers(chatRoomId)).willReturn(FULL_ROOM_CAPACITY);

        // when
        ChatRoomErrorException exception = assertThrows(ChatRoomErrorException.class, () -> chatMemberJoinService.execute(userId, chatRoomId, null));

        // then
        assertEquals(ChatRoomErrorCode.FULL_CHAT_ROOM, exception.getBaseErrorCode());
    }

    @Test
    @DisplayName("비공개 채팅방의 비밀번호가 일치하지 않을 때 가입에 실패한다.")
    void failWhenPasswordIsNotMatch() {
        // given
        Integer invalidPassword = 134679;
        given(chatRoomService.readChatRoom(chatRoomId)).willReturn(Optional.of(createPrivateRoom()));
        given(chatMemberService.countActiveMembers(chatRoomId)).willReturn(AVAILABLE_CAPACITY);

        // when
        ChatRoomErrorException exception = assertThrows(ChatRoomErrorException.class, () -> chatMemberJoinService.execute(userId, chatRoomId, invalidPassword));

        // then
        assertEquals(ChatRoomErrorCode.INVALID_PASSWORD, exception.getBaseErrorCode());
    }

    @Test
    @DisplayName("채팅방 수용 인원이 남아있고, 공개 채팅방이라면 비밀번호 검증을 수행하지 않는다.")
    void successWhenChatRoomIsNotFullAndPublic() {
        // given
        ChatRoom expectedChatRoom = createPublicRoom();
        User expectedUser = createUser();

        given(chatRoomService.readChatRoom(chatRoomId)).willReturn(Optional.of(expectedChatRoom));
        given(chatMemberService.countActiveMembers(chatRoomId)).willReturn(AVAILABLE_CAPACITY);

        given(userService.readUser(userId)).willReturn(Optional.of(expectedUser));
        given(chatMemberService.createMember(expectedUser, expectedChatRoom)).willReturn(ChatMemberFixture.MEMBER.toEntity(expectedUser, expectedChatRoom));

        // when
        chatMemberJoinService.execute(userId, chatRoomId, null);

        // then
        verify(eventPublisher, times(1)).publishEvent(any(ChatRoomJoinEvent.class));
    }

    @Test
    @DisplayName("채팅방 수용 인원이 남아있고, 비밀번호가 일치할 때 가입에 성공한다. 이 때, 가입에 성공한 경우 채팅방 정보와 현재 가입한 회원 수를 반환한다.")
    void successWhenChatRoomIsNotFullAndPasswordIsMatch() {
        // given
        ChatRoom expectedChatRoom = createPrivateRoom();
        User expectedUser = createUser();
        ChatMember expectedMember = ChatMemberFixture.MEMBER.toEntity(expectedUser, expectedChatRoom);
        Integer validPassword = expectedChatRoom.getPassword();

        given(chatRoomService.readChatRoom(chatRoomId)).willReturn(Optional.of(expectedChatRoom));
        given(chatMemberService.countActiveMembers(chatRoomId)).willReturn(AVAILABLE_CAPACITY);

        given(userService.readUser(userId)).willReturn(Optional.of(expectedUser));
        given(chatMemberService.createMember(expectedUser, expectedChatRoom)).willReturn(expectedMember);

        // when
        Pair<ChatRoom, Integer> result = chatMemberJoinService.execute(userId, chatRoomId, validPassword);

        // then
        assertAll(
                () -> assertEquals(expectedChatRoom, result.getLeft()),
                () -> assertEquals(FULL_ROOM_CAPACITY, result.getRight().longValue()),
                () -> verify(eventPublisher, times(1)).publishEvent(any(ChatRoomJoinEvent.class))
        );
    }

    @Test
    @DisplayName("가입 성공 시 정확한 이벤트가 발행된다")
    void explicitEventPublishedWhenJoinSuccess() {
        // given
        ChatRoom expectedChatRoom = createPrivateRoom();
        User expectedUser = createUser();
        ChatMember expectedMember = ChatMemberFixture.MEMBER.toEntity(expectedUser, expectedChatRoom);
        Integer validPassword = expectedChatRoom.getPassword();

        given(chatRoomService.readChatRoom(chatRoomId)).willReturn(Optional.of(expectedChatRoom));
        given(chatMemberService.countActiveMembers(chatRoomId)).willReturn(AVAILABLE_CAPACITY);

        given(userService.readUser(userId)).willReturn(Optional.of(expectedUser));
        given(chatMemberService.createMember(expectedUser, expectedChatRoom)).willReturn(expectedMember);

        // when
        chatMemberJoinService.execute(userId, chatRoomId, validPassword);

        // then
        ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());

        Object capturedEvent = eventCaptor.getValue();
        assertInstanceOf(ChatRoomJoinEvent.class, capturedEvent);
        ChatRoomJoinEvent joinEvent = (ChatRoomJoinEvent) capturedEvent;

        assertAll(
                () -> assertEquals(chatRoomId, joinEvent.chatRoomId()),
                () -> assertEquals(expectedUser.getName(), joinEvent.userName())
        );
    }

    @Test
    @DisplayName("채팅방 가입 시 정해진 순서대로 검증이 수행된다")
    void verifyValidationOrder() {
        // given
        InOrder inOrder = inOrder(chatRoomService, chatMemberService, userService);

        ChatRoom expectedChatRoom = createPrivateRoom();
        User expectedUser = createUser();
        ChatMember expectedMember = ChatMemberFixture.MEMBER.toEntity(expectedUser, expectedChatRoom);
        Integer validPassword = expectedChatRoom.getPassword();

        given(chatRoomService.readChatRoom(chatRoomId)).willReturn(Optional.of(expectedChatRoom));
        given(chatMemberService.countActiveMembers(chatRoomId)).willReturn(AVAILABLE_CAPACITY);

        given(userService.readUser(userId)).willReturn(Optional.of(expectedUser));
        given(chatMemberService.createMember(expectedUser, expectedChatRoom)).willReturn(expectedMember);

        // when
        chatMemberJoinService.execute(userId, chatRoomId, validPassword);

        // then
        inOrder.verify(chatRoomService).readChatRoom(chatRoomId);
        inOrder.verify(chatMemberService).countActiveMembers(chatRoomId);
        inOrder.verify(userService).readUser(userId);
    }

    @Test
    @DisplayName("존재하지 않는 채팅방에 가입을 시도하면 실패한다")
    void failWhenChatRoomNotFound() {
        // given
        given(chatRoomService.readChatRoom(chatRoomId)).willReturn(Optional.empty());

        // when - then
        assertThrows(ChatRoomErrorException.class, () -> chatMemberJoinService.execute(userId, chatRoomId, null));
    }

    @Test
    @DisplayName("존재하지 않는 사용자가 가입을 시도하면 실패한다")
    void failWhenUserNotFound() {
        // given
        given(chatRoomService.readChatRoom(chatRoomId)).willReturn(Optional.of(createPublicRoom()));
        given(chatMemberService.countActiveMembers(chatRoomId)).willReturn(AVAILABLE_CAPACITY);
        given(userService.readUser(userId)).willReturn(Optional.empty());

        // when - then
        assertThrows(UserErrorException.class, () -> chatMemberJoinService.execute(userId, chatRoomId, null));
    }

    private ChatRoom createPublicRoom() {
        return ChatRoomFixture.PUBLIC_CHAT_ROOM.toEntity(1L);
    }

    private ChatRoom createPrivateRoom() {
        return ChatRoomFixture.PRIVATE_CHAT_ROOM.toEntity(1L);
    }

    private User createUser() {
        return UserFixture.GENERAL_USER.toUser();
    }
}
