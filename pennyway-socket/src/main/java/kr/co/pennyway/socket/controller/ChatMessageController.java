package kr.co.pennyway.socket.controller;

import kr.co.pennyway.socket.command.SendMessageCommand;
import kr.co.pennyway.socket.common.annotation.PreAuthorize;
import kr.co.pennyway.socket.common.security.authenticate.UserPrincipal;
import kr.co.pennyway.socket.dto.ChatMessageDto;
import kr.co.pennyway.socket.service.ChatMessageSendService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatMessageController {
    private final ChatMessageSendService chatMessageSendService;

    @MessageMapping("chat.message.{chatRoomId}")
    @PreAuthorize("#isAuthenticated(#principal) and @chatRoomAccessChecker.hasPermission(#chatRoomId, #principal)")
    public void sendMessage(@DestinationVariable Long chatRoomId, @Validated ChatMessageDto.Request payload, UserPrincipal principal) {
        chatMessageSendService.execute(SendMessageCommand.createUserMessage(chatRoomId, payload.content(), payload.contentType(), principal.getUserId()));
    }
}
