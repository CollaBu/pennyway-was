package kr.co.pennyway.api.apis.chat.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.SchemaProperty;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.co.pennyway.api.apis.chat.dto.ChatRoomReq;
import kr.co.pennyway.api.apis.chat.dto.ChatRoomRes;
import kr.co.pennyway.api.common.annotation.ApiExceptionExplanation;
import kr.co.pennyway.api.common.annotation.ApiResponseExplanations;
import kr.co.pennyway.api.common.security.authentication.SecurityUserDetails;
import kr.co.pennyway.domain.common.redis.chatroom.PendedChatRoomErrorCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "[채팅방 API]")
public interface ChatRoomApi {
    @Operation(summary = "[1] 채팅방 생성 대기 요청", method = "POST", description = "채팅방 배경 이미지를 제외한 모든 정보를 서버에 저장하기 위한 API. 성공 시, 채팅방 아이디를 반환하며, 5분 후에 저장된 자동 삭제된다.")
    @ApiResponse(responseCode = "200", description = "채팅방 생성 대기 요청 성공", content = @Content(schemaProperties = @SchemaProperty(name = "chatRoomId", schema = @Schema(implementation = Long.class))))
    ResponseEntity<?> postChatRoom(@RequestBody ChatRoomReq.Pend request, @AuthenticationPrincipal SecurityUserDetails user);

    @Operation(summary = "[2] 채팅방 생성", method = "POST", description = "채팅방 배경 이미지를 전달하여 `[1] 채팅방 생성 대기 요청`의 요청을 확정짓는다. 저장된 채팅방 정보를 반환한다.")
    @ApiResponse(responseCode = "200", description = "채팅방 생성 성공", content = @Content(schemaProperties = @SchemaProperty(name = "chatRoom", schema = @Schema(implementation = ChatRoomRes.Detail.class))))
    @ApiResponseExplanations(errors = {
            @ApiExceptionExplanation(value = PendedChatRoomErrorCode.class, constant = "NOT_FOUND", name = "채팅방 정보 탐색 실패", description = "사용자가 생성한 채팅방 정보가 존재하지 않는 경우 발생하며, 이 경우 채팅방 생성 요청은 재시도 없이 실패 처리해야 한다.")
    })
    ResponseEntity<?> createChatRoom(@RequestBody ChatRoomReq.Create request, @AuthenticationPrincipal SecurityUserDetails user);
}
