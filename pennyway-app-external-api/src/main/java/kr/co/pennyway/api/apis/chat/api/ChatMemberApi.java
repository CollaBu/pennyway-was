package kr.co.pennyway.api.apis.chat.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.SchemaProperty;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotEmpty;
import kr.co.pennyway.api.apis.chat.dto.ChatMemberReq;
import kr.co.pennyway.api.apis.chat.dto.ChatMemberRes;
import kr.co.pennyway.api.apis.chat.dto.ChatRoomRes;
import kr.co.pennyway.api.common.annotation.ApiExceptionExplanation;
import kr.co.pennyway.api.common.annotation.ApiResponseExplanations;
import kr.co.pennyway.api.common.exception.ApiErrorCode;
import kr.co.pennyway.api.common.security.authentication.SecurityUserDetails;
import kr.co.pennyway.domain.domains.chatroom.exception.ChatRoomErrorCode;
import kr.co.pennyway.domain.domains.member.exception.ChatMemberErrorCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Set;

@Tag(name = "[채팅방 멤버 API]")
public interface ChatMemberApi {
    @Operation(summary = "채팅방 멤버 가입", method = "POST", description = "채팅방에 멤버로 가입한다.")
    @Parameters({
            @Parameter(name = "chatRoomId", description = "채팅방 ID", required = true, in = ParameterIn.PATH),
            @Parameter(name = "payload", description = "채팅방 멤버 가입 요청 DTO", required = true, in = ParameterIn.DEFAULT, schema = @Schema(implementation = ChatMemberReq.Join.class))
    })
    @ApiResponse(responseCode = "200", description = "채팅방 멤버 가입 성공", content = @Content(schemaProperties = @SchemaProperty(name = "chatRoom", schema = @Schema(implementation = ChatRoomRes.Detail.class))))
    @ApiResponseExplanations(errors = {
            @ApiExceptionExplanation(value = ChatRoomErrorCode.class, constant = "INVALID_PASSWORD", summary = "비밀번호가 일치하지 않음", description = "비밀번호가 일치하지 않아 채팅방 멤버 가입에 실패했습니다."),
            @ApiExceptionExplanation(value = ChatRoomErrorCode.class, constant = "NOT_FOUND_CHAT_ROOM", summary = "채팅방을 찾을 수 없음", description = "채팅방을 찾을 수 없어 채팅방 멤버 가입에 실패했습니다."),
            @ApiExceptionExplanation(value = ChatRoomErrorCode.class, constant = "FULL_CHAT_ROOM", summary = "채팅방이 가득 참", description = "채팅방이 가득 차서 채팅방 멤버 가입에 실패했습니다."),
            @ApiExceptionExplanation(value = ChatMemberErrorCode.class, constant = "BANNED", summary = "차단된 사용자", description = "차단된 사용자로 채팅방 멤버 가입에 실패했습니다."),
            @ApiExceptionExplanation(value = ChatMemberErrorCode.class, constant = "ALREADY_JOINED", summary = "이미 가입한 사용자", description = "이미 가입한 사용자로 채팅방 멤버 가입에 실패했습니다.")
    })
    ResponseEntity<?> joinChatRoom(
            @PathVariable("chatRoomId") Long chatRoomId,
            @Validated @RequestBody ChatMemberReq.Join payload,
            @AuthenticationPrincipal SecurityUserDetails user
    );

    @Operation(summary = "채팅방 멤버 조회", method = "GET", description = "채팅방 멤버 목록을 조회한다. 오로지 요청자의 채팅방 접근 권한만을 검사하며, 요청 아이디의 채팅방 포함 여부에 대한 검사 및 응답은 포함하지 않는다.")
    @Parameters({
            @Parameter(name = "chatRoomId", description = "채팅방 ID", required = true, in = ParameterIn.PATH),
            @Parameter(name = "ids", description = """
                    멤버 ID 목록. 중복을 허용하며, 순서가 일관되지 않아도 된다. 단, 최대 50개까지 조회 가능하며, null을 허용하지 않는다. 값은 `[채팅방 API] 채팅방 조회`의 응답으로 얻은 `otherParticipantIds`의 값을 사용하면 된다. (주의, userId가 아님!)"""
                    , required = true, in = ParameterIn.QUERY, array = @ArraySchema(schema = @Schema(type = "integer")))
    })
    @ApiResponseExplanations(errors = {
            @ApiExceptionExplanation(value = ApiErrorCode.class, constant = "OVERFLOW_QUERY_PARAMETER", summary = "쿼리 파라미터 오버플로우", description = "쿼리 파라미터가 최대 개수를 초과하여 채팅방 멤버 조회에 실패했습니다.")
    })
    @ApiResponse(responseCode = "200", description = "채팅방 멤버 조회 성공", content = @Content(schemaProperties = @SchemaProperty(name = "chatMembers", array = @ArraySchema(schema = @Schema(implementation = ChatMemberRes.Detail.class)))))
    ResponseEntity<?> readChatMembers(@PathVariable("chatRoomId") Long chatRoomId, @Validated @NotEmpty @RequestParam("ids") Set<Long> ids);
}
