package kr.co.pennyway.api.apis.chat.usecase;

import kr.co.pennyway.api.apis.chat.dto.ChatRoomReq;
import kr.co.pennyway.api.apis.chat.dto.ChatRoomRes;
import kr.co.pennyway.api.apis.chat.mapper.ChatRoomMapper;
import kr.co.pennyway.api.apis.chat.service.ChatRoomSaveService;
import kr.co.pennyway.api.apis.chat.service.ChatRoomSearchService;
import kr.co.pennyway.common.annotation.UseCase;
import kr.co.pennyway.domain.domains.chatroom.domain.ChatRoom;
import kr.co.pennyway.domain.domains.chatroom.dto.ChatRoomDetail;
import lombok.RequiredArgsConstructor;

import java.util.List;

@UseCase
@RequiredArgsConstructor
public class ChatRoomUseCase {
    private final ChatRoomSaveService chatRoomSaveService;
    private final ChatRoomSearchService chatRoomSearchService;

    public Long pendChatRoom(ChatRoomReq.Pend request, Long userId) {
        return chatRoomSaveService.pendChatRoom(request, userId);
    }

    public ChatRoomRes.Detail createChatRoom(ChatRoomReq.Create request, Long userId) {
        ChatRoom chatRoom = chatRoomSaveService.createChatRoom(request, userId);

        return ChatRoomMapper.toChatRoomResDetail(chatRoom, 1);
    }

    public List<ChatRoomRes.Detail> getChatRooms(Long userId) {
        List<ChatRoomDetail> chatRooms = chatRoomSearchService.readChatRooms(userId);

        return ChatRoomMapper.toChatRoomResDetailList(chatRooms);
    }
}
