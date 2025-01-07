package kr.co.pennyway.domain.context.chat.collection;

import kr.co.pennyway.domain.context.common.fixture.ChatRoomFixture;
import kr.co.pennyway.domain.context.common.fixture.UserFixture;
import kr.co.pennyway.domain.domains.chatroom.domain.ChatRoom;
import kr.co.pennyway.domain.domains.member.domain.ChatMember;
import kr.co.pennyway.domain.domains.member.exception.ChatMemberErrorCode;
import kr.co.pennyway.domain.domains.member.exception.ChatMemberErrorException;
import kr.co.pennyway.domain.domains.member.type.ChatMemberRole;
import kr.co.pennyway.domain.domains.user.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

public class ChatRoomLeaveCollectionTest {
    @Test
    @DisplayName("일반 멤버는 언제든지 퇴장할 수 있다")
    void normalMemberShouldBeAbleToLeaveAnytime() {
        // given
        User user = UserFixture.GENERAL_USER.toUser();
        ChatRoom chatRoom = ChatRoomFixture.PUBLIC_CHAT_ROOM.toEntity();
        ChatMember normalMember = ChatMember.of(user, chatRoom, ChatMemberRole.MEMBER);

        ChatRoomLeaveCollection collection = new ChatRoomLeaveCollection(normalMember);

        // when
        ChatRoomLeaveCollection.ChatRoomLeaveResult result = collection.leave();

        // then
        assertEquals(normalMember, result.chatMember());
        assertFalse(result.shouldDeleteChatRoom());
        assertNotNull(normalMember.getDeletedAt());
    }

    @Test
    @DisplayName("방장이 혼자 남은 경우 퇴장할 수 있고, 채팅방이 삭제되어야 한다")
    void adminShouldBeAbleToLeaveWhenAloneAndRoomShouldBeDeleted() {
        // given
        User user = UserFixture.GENERAL_USER.toUser();
        ChatRoom chatRoom = ChatRoomFixture.PUBLIC_CHAT_ROOM.toEntity();
        ChatMember adminMember = ChatMember.of(user, chatRoom, ChatMemberRole.ADMIN);

        ChatRoomLeaveCollection collection = new ChatRoomLeaveCollection(adminMember);

        // when
        ChatRoomLeaveCollection.ChatRoomLeaveResult result = collection.leave();

        // then
        assertEquals(adminMember, result.chatMember());
        assertTrue(result.shouldDeleteChatRoom());
        assertNotNull(adminMember.getDeletedAt());
    }

    @Test
    @DisplayName("다른 멤버가 있는 경우 방장은 퇴장할 수 없다")
    void adminShouldNotBeAbleToLeaveWhenOtherMembersExist() {
        // given
        User admin = UserFixture.GENERAL_USER.toUser();
        User normal = UserFixture.GENERAL_USER.toUser();
        ChatRoom chatRoom = ChatRoomFixture.PUBLIC_CHAT_ROOM.toEntity();
        ChatMember adminMember = ChatMember.of(admin, chatRoom, ChatMemberRole.ADMIN);
        ChatMember normalMember = ChatMember.of(normal, chatRoom, ChatMemberRole.MEMBER);

        ChatRoomLeaveCollection collection = new ChatRoomLeaveCollection(adminMember);

        // when
        assertThatThrownBy(collection::leave)
                .isInstanceOf(ChatMemberErrorException.class)
                .hasFieldOrPropertyWithValue("chatMemberErrorCode", ChatMemberErrorCode.ADMIN_CANNOT_LEAVE);

        // then
        assertNull(adminMember.getDeletedAt());
    }
}
