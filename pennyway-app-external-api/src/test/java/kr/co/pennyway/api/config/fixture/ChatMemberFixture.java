package kr.co.pennyway.api.config.fixture;

import kr.co.pennyway.domain.domains.chatroom.domain.ChatRoom;
import kr.co.pennyway.domain.domains.member.domain.ChatMember;
import kr.co.pennyway.domain.domains.member.type.ChatMemberRole;
import kr.co.pennyway.domain.domains.user.domain.User;

public enum ChatMemberFixture {
    ADMIN(ChatMemberRole.ADMIN),
    MEMBER(ChatMemberRole.MEMBER),
    ;

    private final ChatMemberRole role;

    ChatMemberFixture(ChatMemberRole role) {
        this.role = role;
    }

    public ChatMember toEntity(User user, ChatRoom chatRoom) {
        return ChatMember.of(user, chatRoom, this.role);
    }
}
