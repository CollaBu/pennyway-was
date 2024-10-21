package kr.co.pennyway.api.apis.chat.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.SchemaProperty;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.co.pennyway.api.apis.chat.dto.ChatRoomReq;
import kr.co.pennyway.api.apis.chat.dto.ChatRoomRes;
import kr.co.pennyway.api.common.security.authentication.SecurityUserDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "[채팅방 API]")
public interface ChatRoomApi {
    @Operation(summary = "채팅방 생성", method = "POST", description = "채팅방 생성에 성공하면 생성된 채팅방의 상세 정보를 반환한다.")
    @ApiResponse(responseCode = "200", description = "채팅방 생성 성공", content = @Content(schemaProperties = @SchemaProperty(name = "chatRoom", schema = @Schema(implementation = ChatRoomRes.Detail.class))))
    ResponseEntity<?> createChatRoom(@RequestBody ChatRoomReq.Create request, @AuthenticationPrincipal SecurityUserDetails user);

    @Operation(summary = "가입한 채팅방 목록 조회", method = "GET", description = "사용자가 가입한 채팅방 목록을 조회하며, 정렬 순서는 보장하지 않는다. 최근 활성화된 채팅방의 순서를 지정할 방법에 대해 추가 개선이 필요한 API이므로, 추후 기능이 일부 수정될 수도 있다.")
    @ApiResponse(responseCode = "200", description = "가입한 채팅방 목록 조회 성공", content = @Content(schemaProperties = @SchemaProperty(name = "chatRooms", array = @ArraySchema(schema = @Schema(implementation = ChatRoomRes.Detail.class)))))
    ResponseEntity<?> getMyChatRooms(@AuthenticationPrincipal SecurityUserDetails user);
}
