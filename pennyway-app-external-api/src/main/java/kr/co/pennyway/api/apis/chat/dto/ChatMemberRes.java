package kr.co.pennyway.api.apis.chat.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import kr.co.pennyway.domain.domains.member.dto.ChatMemberResult;
import kr.co.pennyway.domain.domains.member.type.ChatMemberRole;

import java.time.LocalDateTime;

public final class ChatMemberRes {
    @Schema(description = "채팅방 참여자 상세 정보")
    public record MemberDetail(
            @Schema(description = "채팅방 참여자 ID", type = "long")
            Long id,
            @Schema(description = "채팅방 사용자의 애플리케이션 내 고유 식별자 (userId)")
            Long userId,
            @Schema(description = "채팅방 참여자 이름")
            String name,
            @Schema(description = "채팅방 참여자 역할")
            ChatMemberRole role,
            @Schema(description = "채팅방 참여자 알림 설정 여부. 내 정보를 조회할 때만 포함됩니다.")
            @JsonInclude(JsonInclude.Include.NON_NULL)
            Boolean notifyEnabled,
            @Schema(description = "채팅방 가입일")
            @JsonSerialize(using = LocalDateTimeSerializer.class)
            @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
            LocalDateTime createdAt
    ) {
        public static MemberDetail from(ChatMemberResult.Detail chatMember, boolean isContainNotifyEnabled) {
            return new MemberDetail(
                    chatMember.id(),
                    chatMember.userId(),
                    chatMember.name(),
                    chatMember.role(),
                    isContainNotifyEnabled ? chatMember.notifyEnabled() : null,
                    chatMember.createdAt()
            );
        }
    }

    @Schema(description = "채팅방 참여자 요약 정보")
    public record MemberSummary(
            @Schema(description = "채팅방 참여자 ID", type = "long")
            Long id,
            @Schema(description = "채팅방 참여자 이름")
            String name
    ) {
        public static MemberSummary from(ChatMemberResult.Summary chatMember) {
            return new MemberSummary(
                    chatMember.id(),
                    String.valueOf(chatMember.name())
            );
        }
    }
}
