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
    @ApiResponse(responseCode = "200", description = "채팅방 멤버 조회 성공", content = @Content(schemaProperties = @SchemaProperty(name = "chatMembers", array = @ArraySchema(schema = @Schema(implementation = ChatMemberRes.MemberDetail.class)))))
    ResponseEntity<?> readChatMembers(@PathVariable("chatRoomId") Long chatRoomId, @Validated @NotEmpty @RequestParam("ids") Set<Long> ids);

    @Operation(summary = "채팅방 멤버 탈퇴", method = "DELETE", description = "채팅방에서 탈퇴한다. 채팅방장은 채팅 멤버가 한 명이라도 남아있으면 탈퇴할 수 없으며, 채팅방장이 탈퇴할 경우 채팅방이 삭제된다.")
    @Parameter(name = "chatRoomId", description = "채팅방 ID", required = true, in = ParameterIn.PATH)
    @ApiResponseExplanations(errors = {
            @ApiExceptionExplanation(value = ChatMemberErrorCode.class, constant = "ADMIN_CANNOT_LEAVE", summary = "채팅방장은 탈퇴할 수 없음", description = "채팅방장은 채팅방 멤버 탈퇴에 실패했습니다.")}
    )
    @ApiResponse(responseCode = "200", description = "채팅방 멤버 탈퇴 성공")
    ResponseEntity<?> leaveChatRoom(@PathVariable("chatRoomId") Long chatRoomId, @AuthenticationPrincipal SecurityUserDetails user);

    @Operation(summary = "채팅방 멤버 추방", method = "DELETE", description = "채팅방 멤버를 추방한다. 채팅방장만이 채팅방 멤버를 추방할 수 있다.")
    @Parameters({
            @Parameter(name = "chatRoomId", description = "채팅방 ID", required = true, in = ParameterIn.PATH),
            @Parameter(name = "chatMemberId", description = "추방할 채팅방 멤버 ID (user id가 아님)", required = true, in = ParameterIn.PATH)
    })
    @ApiResponseExplanations(errors = {
            @ApiExceptionExplanation(value = ChatMemberErrorCode.class, constant = "NOT_FOUND", summary = "채팅방 멤버를 찾을 수 없음", description = "채팅방 멤버를 찾을 수 없어 채팅방 멤버 추방에 실패했습니다."),
            @ApiExceptionExplanation(value = ChatMemberErrorCode.class, constant = "NOT_ADMIN", summary = "권한 없음", description = "권한이 없어 채팅방 멤버 추방에 실패했습니다.")
    })
    @ApiResponse(responseCode = "200", description = "채팅방 멤버 추방 성공")
    ResponseEntity<?> banChatMember(@PathVariable("chatRoomId") Long chatRoomId, @PathVariable("chatMemberId") Long chatMemberId, @AuthenticationPrincipal SecurityUserDetails user);

    @Operation(summary = "채팅방 관리자 위임", method = "PATCH", description = "채팅방 관리자를 위임한다. 채팅방장만이 채팅방 관리자를 위임할 수 있다.")
    @Parameters({
            @Parameter(name = "chatRoomId", description = "채팅방 ID", required = true, in = ParameterIn.PATH),
            @Parameter(name = "chatMemberId", description = "위임할 채팅방 멤버 ID (user id가 아님)", required = true, in = ParameterIn.PATH)
    })
    @ApiResponseExplanations(errors = {
            @ApiExceptionExplanation(value = ChatMemberErrorCode.class, constant = "NOT_FOUND", summary = "채팅방 멤버를 찾을 수 없음", description = "채팅방 멤버를 찾을 수 없어 채팅방 관리자 위임에 실패했습니다."),
            @ApiExceptionExplanation(value = ChatMemberErrorCode.class, constant = "NOT_ADMIN", summary = "권한 없음", description = "권한이 없어 채팅방 관리자 위임에 실패했습니다."),
            @ApiExceptionExplanation(value = ChatMemberErrorCode.class, constant = "NOT_SAME_CHAT_ROOM", summary = "다른 채팅방 멤버", description = "위임할 채팅방 멤버가 다른 채팅방 멤버여서 채팅방 관리자 위임에 실패했습니다.")
    })
    @ApiResponse(responseCode = "200", description = "채팅방 관리자 위임 성공")
    ResponseEntity<?> delegateAdmin(@PathVariable("chatRoomId") Long chatRoomId, @PathVariable("chatMemberId") Long chatMemberId, @AuthenticationPrincipal SecurityUserDetails user);
}
