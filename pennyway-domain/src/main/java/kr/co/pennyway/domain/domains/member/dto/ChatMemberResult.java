package kr.co.pennyway.domain.domains.member.dto;

import kr.co.pennyway.domain.domains.member.type.ChatMemberRole;

import java.time.LocalDateTime;

public final class ChatMemberResult {
    public record Detail(
            Long id,
            String name,
            ChatMemberRole role,
            boolean notifyEnabled,
            Long userId,
            LocalDateTime createdAt
    ) {
    }

    public record Summary(
            Long id,
            Long name
    ) {
    }
}
