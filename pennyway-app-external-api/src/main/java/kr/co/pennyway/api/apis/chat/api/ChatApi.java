package kr.co.pennyway.api.apis.chat.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.SchemaProperty;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.co.pennyway.api.common.response.SliceResponseTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "[채팅 API]")
public interface ChatApi {
    @Operation(summary = "채팅 조회", description = "채팅방의 채팅 이력을 무한스크롤 조회합니다. 결과는 최신순으로 정렬되며, `lastMessageId`를 포함하지 않은 이전 메시지들을 조회합니다.<br/> content는 채팅방 조회의 `recentMessages` 필드와 동일합니다.")
    @Parameters({
            @Parameter(name = "chatRoomId", description = "채팅방 ID", required = true, in = ParameterIn.PATH),
            @Parameter(name = "lastMessageId", description = "마지막으로 읽은 메시지 ID", required = true, in = ParameterIn.QUERY),
            @Parameter(name = "size", description = "조회할 채팅 수(default: 30)", example = "30", required = false, in = ParameterIn.QUERY)
    })
    @ApiResponse(responseCode = "200", description = "채팅 조회 성공", content = @Content(schemaProperties = @SchemaProperty(name = "chats", schema = @Schema(implementation = SliceResponseTemplate.class))))
    ResponseEntity<?> readChats(
            @PathVariable("chatRoomId") Long chatRoomId,
            @RequestParam(value = "lastMessageId") Long lastMessageId,
            @RequestParam(value = "size", defaultValue = "30") int size
    );
}
