package kr.co.pennyway.api.apis.ledger.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.co.pennyway.api.common.query.SpendingShareType;

import java.util.List;

public class SpendingShareReq {
    @Schema(description = "지출 공유 요청")
    public record ShareQueryParam(
            @Schema(description = "공유 타입 (대/소문자 허용)", example = "chat_room")
            SpendingShareType type,
            int year,
            int month,
            int day,
            @Schema(description = "공유할 채팅방 ID 배열. 공유 타입이 chat_room인 경우 필수", example = "1")
            List<Long> chatRoomIds
    ) {
    }
}
