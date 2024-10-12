package kr.co.pennyway.api.apis.chat.controller;

import kr.co.pennyway.api.apis.chat.dto.ChatRoomReq;
import kr.co.pennyway.api.apis.chat.usecase.ChatRoomUseCase;
import kr.co.pennyway.api.common.response.SuccessResponse;
import kr.co.pennyway.api.common.security.authentication.SecurityUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v2/chat-rooms")
public class ChatRoomController {
    private static final String CHAT_ROOM_ID = "chatRoomId";
    private static final String CHAT_ROOM = "chatRoom";
    private final ChatRoomUseCase chatRoomUseCase;

    @PostMapping("/pend")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> postChatRoom(@RequestBody ChatRoomReq.Pend request, @AuthenticationPrincipal SecurityUserDetails user) {
        return ResponseEntity.ok(SuccessResponse.from(CHAT_ROOM_ID, chatRoomUseCase.pendChatRoom(request, user.getUserId())));
    }

    @PostMapping("")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> createChatRoom(@RequestBody ChatRoomReq.Create request, @AuthenticationPrincipal SecurityUserDetails user) {
        return ResponseEntity.ok(SuccessResponse.from(CHAT_ROOM, chatRoomUseCase.createChatRoom(request, user.getUserId())));
    }
}
