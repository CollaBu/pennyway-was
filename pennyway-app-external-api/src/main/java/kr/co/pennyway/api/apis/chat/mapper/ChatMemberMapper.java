package kr.co.pennyway.api.apis.chat.mapper;

import kr.co.pennyway.api.apis.chat.dto.ChatMemberRes;
import kr.co.pennyway.common.annotation.Mapper;
import kr.co.pennyway.domain.domains.member.dto.ChatMemberResult;

import java.util.List;

@Mapper
public final class ChatMemberMapper {
    public static List<ChatMemberRes.MemberDetail> toChatMemberResDetail(List<ChatMemberResult.Detail> chatMembers, String objectPrefix) {
        return chatMembers.stream()
                .map(chatMember -> createMemberDetail(chatMember, false, objectPrefix))
                .toList();
    }

    private static ChatMemberRes.MemberDetail createMemberDetail(ChatMemberResult.Detail chatMember, boolean isMe, String objectPrefix) {
        return new ChatMemberRes.MemberDetail(
                chatMember.id(),
                chatMember.userId(),
                chatMember.name(),
                chatMember.role(),
                isMe ? chatMember.notifyEnabled() : null,
                chatMember.createdAt(),
                chatMember.profileImageUrl() == null ? "" : objectPrefix + chatMember.profileImageUrl()
        );
    }
}
