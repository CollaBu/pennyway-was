package kr.co.pennyway.api.apis.chat.controller;

import kr.co.pennyway.api.apis.chat.api.ChatApi;
import kr.co.pennyway.api.apis.chat.usecase.ChatUseCase;
import kr.co.pennyway.api.common.response.SuccessResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v2/chat-rooms/{chatRoomId}/chats")
public class ChatController implements ChatApi {
    private static final String CHATS = "chats";

    private final ChatUseCase chatUseCase;

    @Override
    @GetMapping("")
    @PreAuthorize("isAuthenticated() and @chatRoomManager.hasPermission(principal.userId, #chatRoomId)")
    public ResponseEntity<?> readChats(
            @PathVariable("chatRoomId") Long chatRoomId,
            @RequestParam(value = "lastMessageId") Long lastMessageId,
            @RequestParam(value = "size", defaultValue = "30") int size
    ) {
        return ResponseEntity.ok(SuccessResponse.from(CHATS, chatUseCase.readChats(chatRoomId, lastMessageId, size)));
    }
}
