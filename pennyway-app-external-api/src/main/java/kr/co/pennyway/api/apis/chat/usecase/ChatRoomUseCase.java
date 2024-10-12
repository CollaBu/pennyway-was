package kr.co.pennyway.api.apis.chat.usecase;

import kr.co.pennyway.api.apis.chat.dto.ChatRoomReq;
import kr.co.pennyway.api.apis.chat.dto.ChatRoomRes;
import kr.co.pennyway.api.apis.chat.mapper.ChatRoomMapper;
import kr.co.pennyway.api.apis.chat.service.ChatRoomSaveService;
import kr.co.pennyway.common.annotation.UseCase;
import kr.co.pennyway.domain.domains.chatroom.domain.ChatRoom;
import lombok.RequiredArgsConstructor;

@UseCase
@RequiredArgsConstructor
public class ChatRoomUseCase {
    private final ChatRoomSaveService chatRoomSaveService;

    public ChatRoomRes.Detail createChatRoom(ChatRoomReq.Create request, Long userId) {
        ChatRoom chatRoom = chatRoomSaveService.createChatRoom(request, userId);

        return ChatRoomMapper.toChatRoomResDetail(chatRoom, 1);
    }
}
