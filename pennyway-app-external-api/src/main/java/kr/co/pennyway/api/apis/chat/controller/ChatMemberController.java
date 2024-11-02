package kr.co.pennyway.api.apis.chat.controller;

import jakarta.validation.constraints.NotEmpty;
import kr.co.pennyway.api.apis.chat.api.ChatMemberApi;
import kr.co.pennyway.api.apis.chat.dto.ChatMemberReq;
import kr.co.pennyway.api.apis.chat.dto.ChatRoomRes;
import kr.co.pennyway.api.apis.chat.usecase.ChatMemberUseCase;
import kr.co.pennyway.api.common.exception.ApiErrorCode;
import kr.co.pennyway.api.common.exception.ApiErrorException;
import kr.co.pennyway.api.common.response.SuccessResponse;
import kr.co.pennyway.api.common.security.authentication.SecurityUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v2/chat-rooms/{chatRoomId}/chat-members")
public class ChatMemberController implements ChatMemberApi {
    private static final String CHAT_ROOM = "chatRoom";
    private static final String CHAT_MEMBERS = "chatMembers";

    private final ChatMemberUseCase chatMemberUseCase;

    @Override
    @PostMapping("")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> joinChatRoom(
            @PathVariable("chatRoomId") Long chatRoomId,
            @Validated @RequestBody ChatMemberReq.Join payload,
            @AuthenticationPrincipal SecurityUserDetails user
    ) {
        ChatRoomRes.Detail detail = chatMemberUseCase.joinChatRoom(user.getUserId(), chatRoomId, payload.password());

        return ResponseEntity.ok(SuccessResponse.from(CHAT_ROOM, detail));
    }

    @Override
    @GetMapping("")
    @PreAuthorize("isAuthenticated() and @chatRoomManager.hasPermission(principal.userId, #chatRoomId)")
    public ResponseEntity<?> readChatMembers(@PathVariable("chatRoomId") Long chatRoomId, @Validated @NotEmpty @RequestParam("ids") Set<Long> ids) {
        if (ids.size() > 50) {
            throw new ApiErrorException(ApiErrorCode.OVERFLOW_QUERY_PARAMETER);
        }

        return ResponseEntity.ok(SuccessResponse.from(CHAT_MEMBERS, chatMemberUseCase.readChatMembers(chatRoomId, ids)));
    }
}
