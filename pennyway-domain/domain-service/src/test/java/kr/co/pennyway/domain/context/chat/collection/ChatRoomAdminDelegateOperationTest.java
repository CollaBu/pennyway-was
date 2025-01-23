package kr.co.pennyway.domain.context.chat.collection;

import kr.co.pennyway.domain.context.common.fixture.ChatRoomFixture;
import kr.co.pennyway.domain.context.common.fixture.UserFixture;
import kr.co.pennyway.domain.domains.chatroom.domain.ChatRoom;
import kr.co.pennyway.domain.domains.member.domain.ChatMember;
import kr.co.pennyway.domain.domains.member.exception.ChatMemberErrorCode;
import kr.co.pennyway.domain.domains.member.exception.ChatMemberErrorException;
import kr.co.pennyway.domain.domains.member.type.ChatMemberRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ChatRoomAdminDelegateOperationTest {

    @Test
    @DisplayName("방장은 다른 멤버에게 방장 권한을 위임할 수 있다")
    void shouldDelegateAdmin() {
        // given
        var chatRoom = ChatRoomFixture.PUBLIC_CHAT_ROOM.toEntity();

        var chatAdmin = createChatMember(1L, 1L, ChatMemberRole.ADMIN, chatRoom);
        var chatMember = createChatMember(2L, 2L, ChatMemberRole.MEMBER, chatRoom);

        var operation = new ChatRoomAdminDelegateOperation(chatAdmin, chatMember);

        // when
        operation.execute();

        // then
        assertEquals(ChatMemberRole.ADMIN, chatMember.getRole());
        assertEquals(ChatMemberRole.MEMBER, chatAdmin.getRole());
    }

    @Test
    @DisplayName("방장이 아닌 멤버는 방장 권한을 위임할 수 없다")
    void shouldNotDelegateAdmin() {
        // given
        var chatRoom = ChatRoomFixture.PUBLIC_CHAT_ROOM.toEntity();

        var chatAdmin = createChatMember(1L, 1L, ChatMemberRole.MEMBER, chatRoom);
        var chatMember = createChatMember(2L, 2L, ChatMemberRole.MEMBER, chatRoom);

        var operation = new ChatRoomAdminDelegateOperation(chatAdmin, chatMember);

        // when & then
        assertThatThrownBy(operation::execute)
                .isInstanceOf(ChatMemberErrorException.class)
                .hasFieldOrPropertyWithValue("chatMemberErrorCode", ChatMemberErrorCode.NOT_ADMIN);
    }

    @Test
    @DisplayName("방장은 자기자신에게 방장 권한을 위임할 수 없다")
    void shouldNotDelegateAdminToSelf() {
        // given
        var chatRoom = ChatRoomFixture.PUBLIC_CHAT_ROOM.toEntity();
        var chatAdmin = createChatMember(1L, 1L, ChatMemberRole.ADMIN, chatRoom);

        var operation = new ChatRoomAdminDelegateOperation(chatAdmin, chatAdmin);

        // when & then
        assertThatThrownBy(operation::execute)
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("다른 채팅방의 멤버에게 방장 권한을 위임할 수 없다")
    void shouldNotDelegateAdminToMemberInOtherChatRoom() {
        // given
        var chatRoom = ChatRoomFixture.PUBLIC_CHAT_ROOM.toEntityWithId(1L);
        var otherChatRoom = ChatRoomFixture.PUBLIC_CHAT_ROOM.toEntityWithId(2L);

        var chatAdmin = createChatMember(1L, 1L, ChatMemberRole.ADMIN, chatRoom);
        var chatMember = createChatMember(2L, 2L, ChatMemberRole.MEMBER, otherChatRoom);

        var operation = new ChatRoomAdminDelegateOperation(chatAdmin, chatMember);

        // when & then
        assertThatThrownBy(operation::execute)
                .isInstanceOf(IllegalArgumentException.class);
    }

    private ChatMember createChatMember(Long userId, Long chatMemberId, ChatMemberRole role, ChatRoom chatRoom) {
        var user = UserFixture.GENERAL_USER.toUser();
        ReflectionTestUtils.setField(user, "id", userId);

        var chatMember = ChatMember.of(user, chatRoom, role);
        ReflectionTestUtils.setField(chatMember, "id", chatMemberId);

        return chatMember;
    }
}
