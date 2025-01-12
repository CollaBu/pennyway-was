package kr.co.pennyway.api.apis.chat.controller;

import kr.co.pennyway.api.apis.chat.api.ChatRoomApi;
import kr.co.pennyway.api.apis.chat.dto.ChatRoomReq;
import kr.co.pennyway.api.apis.chat.usecase.ChatRoomUseCase;
import kr.co.pennyway.api.common.response.SuccessResponse;
import kr.co.pennyway.api.common.security.authentication.SecurityUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v2/chat-rooms")
public class ChatRoomController implements ChatRoomApi {
    private static final String CHAT_ROOM = "chatRoom";
    private static final String CHAT_ROOMS = "chatRooms";
    private final ChatRoomUseCase chatRoomUseCase;

    @Override
    @PostMapping("")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> createChatRoom(@Validated @RequestBody ChatRoomReq.Create request, @AuthenticationPrincipal SecurityUserDetails user) {
        return ResponseEntity.ok(SuccessResponse.from(CHAT_ROOM, chatRoomUseCase.createChatRoom(request, user.getUserId())));
    }

    @Override
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getMyChatRooms(@RequestParam(name = "summary", required = false, defaultValue = "false") boolean isSummary, @AuthenticationPrincipal SecurityUserDetails user) {
        if (isSummary) {
            return ResponseEntity.ok(SuccessResponse.from(CHAT_ROOM, chatRoomUseCase.readJoinedChatRoomIds(user.getUserId())));
        }

        return ResponseEntity.ok(SuccessResponse.from(CHAT_ROOMS, chatRoomUseCase.getChatRooms(user.getUserId())));
    }

    @Override
    @GetMapping("")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> searchChatRooms(@Validated ChatRoomReq.SearchQuery query, @AuthenticationPrincipal SecurityUserDetails user) {
        Pageable pageable = Pageable.ofSize(query.size()).withPage(query.page());

        return ResponseEntity.ok(SuccessResponse.from(CHAT_ROOM, chatRoomUseCase.searchChatRooms(user.getUserId(), query.target(), pageable)));
    }

    @Override
    @GetMapping("/{chatRoomId}")
    @PreAuthorize("isAuthenticated() and @chatRoomManager.hasPermission(#user.getUserId(), #chatRoomId)")
    public ResponseEntity<?> getChatRoom(@PathVariable("chatRoomId") Long chatRoomId, @AuthenticationPrincipal SecurityUserDetails user) {
        return ResponseEntity.ok(SuccessResponse.from(CHAT_ROOM, chatRoomUseCase.getChatRoomWithParticipants(user.getUserId(), chatRoomId)));
    }

    @PutMapping("/{chatRoomId}")
    @PreAuthorize("isAuthenticated() and @chatRoomManager.hasAdminPermission(principal.userId, #chatRoomId)")
    public ResponseEntity<?> updateChatRoom(@PathVariable("chatRoomId") Long chatRoomId, @Validated @RequestBody ChatRoomReq.Update request) {
        return ResponseEntity.ok(SuccessResponse.from(CHAT_ROOM, chatRoomUseCase.updateChatRoom(request)));
    }
}
