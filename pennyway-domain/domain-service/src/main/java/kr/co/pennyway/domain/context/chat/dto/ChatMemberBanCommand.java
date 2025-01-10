package kr.co.pennyway.domain.context.chat.dto;

public record ChatMemberBanCommand(
        Long adminId,
        Long targetMemberId
) {
    public ChatMemberBanCommand {
        if (adminId == null) {
            throw new IllegalArgumentException("adminId must not be null");
        }
        if (targetMemberId == null) {
            throw new IllegalArgumentException("targetMemberId must not be null");
        }
    }

    public static ChatMemberBanCommand of(Long adminId, Long targetMemberId) {
        return new ChatMemberBanCommand(adminId, targetMemberId);
    }
}
