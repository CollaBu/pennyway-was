package kr.co.pennyway.domain.common.converter;

import jakarta.persistence.Converter;
import kr.co.pennyway.domain.domains.member.type.ChatMemberRole;

@Converter
public class ChatMemberRoleConverter extends AbstractLegacyEnumAttributeConverter<ChatMemberRole> {
    private static final String ENUM_NAME = "채팅방 멤버 역할";

    public ChatMemberRoleConverter() {
        super(ChatMemberRole.class, false, ENUM_NAME);
    }
}
