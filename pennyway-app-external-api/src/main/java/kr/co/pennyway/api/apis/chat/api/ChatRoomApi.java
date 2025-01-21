package kr.co.pennyway.api.apis.chat.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.SchemaProperty;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.co.pennyway.api.apis.chat.dto.ChatRoomReq;
import kr.co.pennyway.api.apis.chat.dto.ChatRoomRes;
import kr.co.pennyway.api.common.annotation.ApiExceptionExplanation;
import kr.co.pennyway.api.common.annotation.ApiResponseExplanations;
import kr.co.pennyway.api.common.response.SliceResponseTemplate;
import kr.co.pennyway.api.common.security.authentication.SecurityUserDetails;
import kr.co.pennyway.domain.domains.member.exception.ChatMemberErrorCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "[채팅방 API]")
public interface ChatRoomApi {
    @Operation(summary = "채팅방 생성", method = "POST", description = "채팅방 생성에 성공하면 생성된 채팅방의 상세 정보를 반환한다.")
    @ApiResponse(responseCode = "200", description = "채팅방 생성 성공", content = @Content(schemaProperties = @SchemaProperty(name = "chatRoom", schema = @Schema(implementation = ChatRoomRes.Detail.class))))
    ResponseEntity<?> createChatRoom(@RequestBody ChatRoomReq.Create request, @AuthenticationPrincipal SecurityUserDetails user);

    @Operation(summary = "가입한 채팅방 목록 조회", method = "GET", description = "사용자가 가입한 채팅방 목록을 조회하며, 정렬 순서는 보장하지 않는다. 최근 활성화된 채팅방의 순서를 지정할 방법에 대해 추가 개선이 필요한 API이므로, 추후 기능이 일부 수정될 수도 있다.")
    @Parameter(name = "summary", description = "채팅방 요약 정보 조회 여부. true로 설정하면 채팅방의 상세 정보가 chatRoomIds 필드만 반환된다. (default=false)", example = "false")
    @ApiResponse(responseCode = "200", description = "가입한 채팅방 목록 조회 성공", content = @Content(schemaProperties = @SchemaProperty(name = "chatRooms", array = @ArraySchema(schema = @Schema(implementation = ChatRoomRes.Detail.class)))))
    ResponseEntity<?> getMyChatRooms(@RequestParam(name = "summary", required = false, defaultValue = "false") boolean query, @AuthenticationPrincipal SecurityUserDetails user);

    @Operation(summary = "채팅방 검색", method = "GET", description = "사용자가 가입한 채팅방 중 검색어에 일치하는 채팅방 목록을 조회한다. 검색 결과는 무한 스크롤 응답으로 반환되며, 정렬 순서는 정확도가 높은 순으로 반환된다. contents 필드는 List<ChatRoomRes.Detail> 타입으로, '가입한 채팅방 목록 조회' API 응답과 동일하다.")
    @Parameters({
            @Parameter(name = "target", description = "검색 대상. 채팅방 제목 혹은 설명을 검색한다. 최소한 2자 이상의 문자열이어야 한다.", example = "페니웨이", required = true),
            @Parameter(name = "page", description = "페이지 번호. 0부터 시작한다.", example = "0", required = true),
            @Parameter(name = "size", description = "페이지 크기. 한 페이지 당 반환되는 채팅방 개수이다. 기본값으로 10개씩 반환한다."),
            @Parameter(name = "query", hidden = true)
    })
    @ApiResponse(responseCode = "200", description = "채팅방 검색 성공", content = @Content(schemaProperties = @SchemaProperty(name = "chatRooms", schema = @Schema(implementation = SliceResponseTemplate.class))))
    ResponseEntity<?> searchChatRooms(@Validated ChatRoomReq.SearchQuery query, @AuthenticationPrincipal SecurityUserDetails user);

    @Operation(summary = "채팅방 상세 조회", method = "GET", description = "사용자가 가입한 채팅방 중 특정 채팅방의 상세 정보를 조회한다. 채팅방의 상세 정보에는 채팅방의 참여자 목록과 최근 채팅 메시지 목록 등이 포함된다.")
    @Parameter(name = "chatRoomId", description = "조회할 채팅방의 식별자", example = "1", required = true)
    @ApiResponse(responseCode = "200", description = "채팅방 조회 성공", content = @Content(schemaProperties = @SchemaProperty(name = "chatRoom", schema = @Schema(implementation = ChatRoomRes.RoomWithParticipants.class))))
    ResponseEntity<?> getChatRoom(@PathVariable("chatRoomId") Long chatRoomId, @AuthenticationPrincipal SecurityUserDetails user);

    @Operation(summary = "채팅방 관리자 모드 조회", method = "GET", description = "채팅방 정보를 관리자 모드로 조회한다. 채팅방 비밀번호를 포함하며, 채팅방 방장만이 조회 가능하다.")
    @Parameter(name = "chatRoomId", description = "조회할 채팅방의 식별자", example = "1", required = true)
    @ApiResponse(responseCode = "200", description = "채팅방 관리자 모드 조회 성공", content = @Content(schemaProperties = @SchemaProperty(name = "chatRoom", schema = @Schema(implementation = ChatRoomRes.AdminView.class))))
    ResponseEntity<?> getChatRoomAdmin(@PathVariable("chatRoomId") Long chatRoomId);

    @Operation(summary = "채팅방 수정", method = "PUT", description = "채팅방의 정보를 수정한다. 채팅방의 정보 수정에 성공하면 수정된 채팅방의 상세 정보를 반환한다.")
    @Parameter(name = "chatRoomId", description = "수정할 채팅방의 식별자", example = "1", required = true)
    @ApiResponse(responseCode = "200", description = "채팅방 수정 성공", content = @Content(schemaProperties = @SchemaProperty(name = "chatRoom", schema = @Schema(implementation = ChatRoomRes.Detail.class))))
    ResponseEntity<?> updateChatRoom(@PathVariable("chatRoomId") Long chatRoomId, @Validated @RequestBody ChatRoomReq.Update request);

    @Operation(summary = "채팅방 삭제", method = "DELETE", description = "채팅방을 삭제한다. 채팅방 방장만이 가능하며, 채팅방을 삭제하면 채팅방에 참여한 모든 사용자가 채팅방에서 나가게 된다.")
    @Parameter(name = "chatRoomId", description = "삭제할 채팅방의 식별자", example = "1", required = true)
    @ApiResponseExplanations(errors = {
            @ApiExceptionExplanation(value = ChatMemberErrorCode.class, constant = "NOT_FOUND", summary = "채팅방 멤버 정보를 찾을 수 없음"),
            @ApiExceptionExplanation(value = ChatMemberErrorCode.class, constant = "NOT_ADMIN", summary = "권한 없음", description = "채팅방 방장이 아니라 채팅방을 삭제할 수 없습니다.")
    })
    @ApiResponse(responseCode = "200", description = "채팅방 삭제 성공")
    ResponseEntity<?> deleteChatRoom(@PathVariable("chatRoomId") Long chatRoomId, @AuthenticationPrincipal SecurityUserDetails user);
}
