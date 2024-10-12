package kr.co.pennyway.api.apis.chat.controller;

import kr.co.pennyway.api.apis.chat.dto.ChatRoomRequest;
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
    private ChatRoomUseCase chatRoomUseCase;

    @PostMapping("")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> postChatRoom(@RequestBody ChatRoomRequest.Create request, @AuthenticationPrincipal SecurityUserDetails user) {
        return ResponseEntity.ok(chatRoomUseCase.createChatRoom(request, user.getUserId()));
    }
}
