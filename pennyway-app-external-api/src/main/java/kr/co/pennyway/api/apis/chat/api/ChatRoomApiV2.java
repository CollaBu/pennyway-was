package kr.co.pennyway.api.apis.chat.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.SchemaProperty;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.co.pennyway.api.apis.chat.dto.ChatRoomRes;
import kr.co.pennyway.api.common.security.authentication.SecurityUserDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "[임시 채팅방 API]", description = "버전 관리를 위한 임시 채팅방 API, 추후 [채팅방 API]로 통합될 예정입니다.")
public interface ChatRoomApiV2 {
    @Operation(summary = "가입한 채팅방 목록 조회", method = "GET", description = "사용자가 가입한 채팅방 목록을 조회하며, 정렬 순서는 보장하지 않는다. 최근 활성화된 채팅방의 순서를 지정할 방법에 대해 추가 개선이 필요한 API이므로, 추후 기능이 일부 수정될 수도 있다.")
    @Parameter(name = "summary", description = "채팅방 요약 정보 조회 여부. true로 설정하면 채팅방의 상세 정보가 chatRoomIds 필드만 반환된다. (default=false)", example = "false")
    @ApiResponse(responseCode = "200", description = "가입한 채팅방 목록 조회 성공", content = @Content(schemaProperties = @SchemaProperty(name = "chatRooms", array = @ArraySchema(schema = @Schema(implementation = ChatRoomRes.Detailv2.class)))))
    ResponseEntity<?> getMyChatRooms(@RequestParam(name = "summary", required = false, defaultValue = "false") boolean isSummary, @AuthenticationPrincipal SecurityUserDetails user);
}
