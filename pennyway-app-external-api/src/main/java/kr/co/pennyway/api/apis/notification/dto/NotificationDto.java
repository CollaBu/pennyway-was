package kr.co.pennyway.api.apis.notification.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import kr.co.pennyway.domain.domains.notification.domain.Notification;
import kr.co.pennyway.domain.domains.notification.type.NoticeType;
import lombok.Builder;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public class NotificationDto {
    @Schema(title = "푸시 알림 슬라이스 응답")
    public record SliceRes(
            @Schema(description = "푸시 알림 리스트")
            List<Info> content,
            @Schema(description = "현재 페이지 번호")
            int currentPageNumber,
            @Schema(description = "페이지 크기")
            int pageSize,
            @Schema(description = "전체 요소 개수")
            int numberOfElements,
            @Schema(description = "다음 페이지 존재 여부")
            boolean hasNext
    ) {
        public static SliceRes from(List<Info> notifications, Pageable pageable, int numberOfElements, boolean hasNext) {
            return new SliceRes(notifications, pageable.getPageNumber(), pageable.getPageSize(), numberOfElements, hasNext);
        }
    }

    @Builder
    @Schema(title = "푸시 알림 상세 정보", description = "푸시 알림 pk, 읽음 여부, 제목, 내용, 타입 그리고 딥 링크 정보를 담고 있다.")
    public record Info(
            @Schema(description = "푸시 알림 pk", example = "1")
            Long id,
            @Schema(description = "푸시 알림 읽음 여부", example = "true")
            boolean isRead,
            @Schema(description = "푸시 알림 제목", example = "페니웨이 공지")
            String title,
            @Schema(description = "푸시 알림 내용", example = "안녕하세요. 페니웨이입니다.")
            String content,
            @Schema(description = "푸시 알림 타입. ex) ANNOUNCEMENT", example = "FEED_LIKE_FROM_TO")
            String type,
            @Schema(description = "푸시 알림 행위자. ex) 다른 사용자 <type이 ANNOUNCEMENT면 존재하지 않음>", example = "pennyway")
            @JsonInclude(JsonInclude.Include.NON_NULL)
            String from,
            @Schema(description = "푸시 알림 행위자 pk <type이 ANNOUNCEMENT면 존재하지 않음>", example = "1")
            @JsonInclude(JsonInclude.Include.NON_NULL)
            Long fromId,
            @Schema(description = "푸시 알림 행위자가 액션을 취한 대상 pk. ex) 피드 pk, 댓글 pk <type이 ANNOUNCEMENT면 존재하지 않음>", example = "3")
            @JsonInclude(JsonInclude.Include.NON_NULL)
            Long toId,
            @Schema(description = "푸시 알림 생성 시간", example = "yyyy-MM-dd HH:mm:ss")
            @JsonSerialize(using = LocalDateTimeSerializer.class)
            @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
            LocalDateTime createdAt
    ) {
        public static NotificationDto.Info from(Notification notification) {
            NotificationDto.Info.InfoBuilder builder = NotificationDto.Info.builder()
                    .id(notification.getId())
                    .isRead(notification.getReadAt() != null)
                    .title(notification.createFormattedTitle())
                    .content(notification.createFormattedContent())
                    .type(notification.getType().name())
                    .createdAt(notification.getCreatedAt());

            if (!notification.getType().equals(NoticeType.ANNOUNCEMENT)) {
                builder.from(notification.getSenderName())
                        .fromId(notification.getSender().getId()).toId(notification.getToId());
            }

            return builder.build();
        }
    }
}
