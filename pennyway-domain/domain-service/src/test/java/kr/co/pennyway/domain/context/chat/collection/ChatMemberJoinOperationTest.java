package kr.co.pennyway.domain.context.chat.collection;

import kr.co.pennyway.domain.context.common.fixture.ChatRoomFixture;
import kr.co.pennyway.domain.context.common.fixture.UserFixture;
import kr.co.pennyway.domain.domains.chatroom.domain.ChatRoom;
import kr.co.pennyway.domain.domains.chatroom.exception.ChatRoomErrorCode;
import kr.co.pennyway.domain.domains.chatroom.exception.ChatRoomErrorException;
import kr.co.pennyway.domain.domains.member.domain.ChatMember;
import kr.co.pennyway.domain.domains.member.exception.ChatMemberErrorCode;
import kr.co.pennyway.domain.domains.member.exception.ChatMemberErrorException;
import kr.co.pennyway.domain.domains.member.type.ChatMemberRole;
import kr.co.pennyway.domain.domains.user.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ChatMemberJoinOperationTest {
    private static final long MAX_MEMBER_COUNT = 300L;

    @Test
    @DisplayName("이미 채팅방에 참여한 사용자가 다시 참여할 때 가입에 실패한다")
    void failWhenAlreadyJoined() {
        // given
        var user = createUser(1L);
        var chatRoom = ChatRoomFixture.PUBLIC_CHAT_ROOM.toEntity();
        var chatMembers = List.of(ChatMember.of(user, chatRoom, ChatMemberRole.MEMBER));

        var operation = new ChatMemberJoinOperation(user, chatRoom, chatMembers);

        // when & then
        assertThatThrownBy(() -> operation.execute(chatRoom.getPassword()))
                .isInstanceOf(ChatMemberErrorException.class)
                .hasFieldOrPropertyWithValue("baseErrorCode", ChatMemberErrorCode.ALREADY_JOINED);
    }

    @Test
    @DisplayName("채팅방이 가득 찼을 때 (정원 300명) 가입에 실패한다")
    void failWhenChatRoomIsFull() {
        // given
        var user = createUser(1L);
        var chatRoom = ChatRoomFixture.PUBLIC_CHAT_ROOM.toEntity();
        var chatMembers = IntStream.range(0, 300)
                .mapToObj(i -> createChatMember(i + 2L, chatRoom)) // userId 2~301
                .collect(Collectors.toSet());

        var operation = new ChatMemberJoinOperation(user, chatRoom, chatMembers);

        // when & then
        assertThatThrownBy(() -> operation.execute(chatRoom.getPassword()))
                .isInstanceOf(ChatRoomErrorException.class)
                .hasFieldOrPropertyWithValue("baseErrorCode", ChatRoomErrorCode.FULL_CHAT_ROOM);
    }

    @Test
    @DisplayName("비공개 채팅방의 비밀번호가 일치하지 않을 때 가입에 실패한다")
    void failWhenPasswordIsNotMatch() {
        // given
        var user = createUser(1L);
        var chatRoom = ChatRoomFixture.PRIVATE_CHAT_ROOM.toEntity();
        var chatMembers = new HashSet<ChatMember>();

        var operation = new ChatMemberJoinOperation(user, chatRoom, chatMembers);

        // when & then
        assertThatThrownBy(() -> operation.execute(235676))
                .isInstanceOf(ChatRoomErrorException.class)
                .hasFieldOrPropertyWithValue("baseErrorCode", ChatRoomErrorCode.INVALID_PASSWORD);
    }

    @Test
    @DisplayName("채팅방 수용 인원이 남아있고, 공개 채팅방이라면 비밀번호 검증을 수행하지 않는다")
    void successWhenChatRoomIsNotFullAndPublic() {
        // given
        var user = createUser(1L);
        var chatRoom = ChatRoomFixture.PUBLIC_CHAT_ROOM.toEntity();
        var chatMembers = new HashSet<ChatMember>();

        var operation = new ChatMemberJoinOperation(user, chatRoom, chatMembers);

        // when
        ChatMember result = operation.execute(chatRoom.getPassword());

        // then
        assertAll(
                () -> assertEquals(user.getId(), result.getUserId()),
                () -> assertEquals(chatRoom, result.getChatRoom()),
                () -> assertEquals(ChatMemberRole.MEMBER, result.getRole())
        );
    }

    @Test
    @DisplayName("채팅방 수용 인원이 남아있고, 비공개 채팅방이라면 비밀번호 검증을 수행한다")
    void successWhenChatRoomIsNotFullAndPrivate() {
        // given
        var user = createUser(1L);
        var chatRoom = ChatRoomFixture.PRIVATE_CHAT_ROOM.toEntity();
        var chatMembers = new HashSet<ChatMember>();

        var operation = new ChatMemberJoinOperation(user, chatRoom, chatMembers);

        // when
        ChatMember result = operation.execute(chatRoom.getPassword());

        // then
        assertAll(
                () -> assertEquals(user.getId(), result.getUserId()),
                () -> assertEquals(chatRoom, result.getChatRoom()),
                () -> assertEquals(ChatMemberRole.MEMBER, result.getRole())
        );
    }

    private User createUser(Long userId) {
        var user = UserFixture.GENERAL_USER.toUser();
        ReflectionTestUtils.setField(user, "id", userId);
        return user;
    }

    private ChatMember createChatMember(Long userId, ChatRoom chatRoom) {
        return ChatMember.of(createUser(userId), chatRoom, ChatMemberRole.MEMBER);
    }
}
