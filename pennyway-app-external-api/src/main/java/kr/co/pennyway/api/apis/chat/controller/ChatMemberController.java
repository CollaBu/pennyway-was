package kr.co.pennyway.api.apis.chat.controller;

import kr.co.pennyway.api.apis.chat.api.ChatMemberApi;
import kr.co.pennyway.api.apis.chat.dto.ChatMemberReq;
import kr.co.pennyway.api.apis.chat.dto.ChatRoomRes;
import kr.co.pennyway.api.apis.chat.usecase.ChatMemberUseCase;
import kr.co.pennyway.api.common.response.SuccessResponse;
import kr.co.pennyway.api.common.security.authentication.SecurityUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v2/chat-rooms/{chatRoomId}/chat-members")
public class ChatMemberController implements ChatMemberApi {
    private static final String CHAT_ROOM = "chatRoom";
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
}
