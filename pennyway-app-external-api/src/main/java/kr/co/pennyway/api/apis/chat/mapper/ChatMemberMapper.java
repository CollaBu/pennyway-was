package kr.co.pennyway.api.apis.chat.mapper;

import kr.co.pennyway.api.apis.chat.dto.ChatMemberRes;
import kr.co.pennyway.common.annotation.Mapper;
import kr.co.pennyway.domain.domains.member.domain.ChatMember;

import java.util.List;

@Mapper
public final class ChatMemberMapper {
    public static List<ChatMemberRes.Detail> toChatMemberResDetail(List<ChatMember> chatMembers) {
        return chatMembers.stream()
                .map(chatMember -> ChatMemberRes.Detail.from(chatMember, false))
                .toList();
    }
}
