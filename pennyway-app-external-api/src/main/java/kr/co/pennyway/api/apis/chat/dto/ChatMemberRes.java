package kr.co.pennyway.api.apis.chat.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import kr.co.pennyway.domain.domains.member.domain.ChatMember;
import kr.co.pennyway.domain.domains.member.type.ChatMemberRole;

import java.time.LocalDateTime;

public final class ChatMemberRes {
    @Schema(description = "채팅방 참여자 상세 정보")
    public record Detail(
            @Schema(description = "채팅방 참여자 ID", type = "long")
            Long id,
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
        public static Detail from(ChatMember chatMember, boolean isContainNotifyEnabled) {
            return new Detail(
                    chatMember.getId(),
                    chatMember.getName(),
                    chatMember.getRole(),
                    isContainNotifyEnabled ? chatMember.isNotifyEnabled() : null,
                    chatMember.getCreatedAt()
            );
        }
    }
}
