package kr.co.pennyway.api.apis.chat.controller;

import kr.co.pennyway.api.apis.chat.api.ChatRoomApiV2;
import kr.co.pennyway.api.apis.chat.usecase.ChatRoomUseCase;
import kr.co.pennyway.api.common.response.SuccessResponse;
import kr.co.pennyway.api.common.security.authentication.SecurityUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v3/chat-rooms")
public class ChatRoomControllerV2 implements ChatRoomApiV2 {
    private static final String CHAT_ROOMS = "chatRooms";
    private final ChatRoomUseCase chatRoomUseCase;

    @Override
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getMyChatRooms(@RequestParam(name = "summary", required = false, defaultValue = "false") boolean isSummary, @AuthenticationPrincipal SecurityUserDetails user) {
        if (isSummary) {
            return ResponseEntity.ok(SuccessResponse.from(CHAT_ROOMS, chatRoomUseCase.readJoinedChatRoomIds(user.getUserId())));
        }

        return ResponseEntity.ok(SuccessResponse.from(CHAT_ROOMS, chatRoomUseCase.getChatRoomDetails(user.getUserId())));
    }
}
