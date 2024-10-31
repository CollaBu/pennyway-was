package kr.co.pennyway.api.apis.chat.usecase;

import kr.co.pennyway.api.apis.chat.dto.ChatRoomReq;
import kr.co.pennyway.api.apis.chat.dto.ChatRoomRes;
import kr.co.pennyway.api.apis.chat.mapper.ChatRoomMapper;
import kr.co.pennyway.api.apis.chat.service.ChatMemberSearchService;
import kr.co.pennyway.api.apis.chat.service.ChatRoomSaveService;
import kr.co.pennyway.api.apis.chat.service.ChatRoomSearchService;
import kr.co.pennyway.api.common.response.SliceResponseTemplate;
import kr.co.pennyway.common.annotation.UseCase;
import kr.co.pennyway.domain.domains.chatroom.domain.ChatRoom;
import kr.co.pennyway.domain.domains.chatroom.dto.ChatRoomDetail;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.util.List;
import java.util.Set;

@UseCase
@RequiredArgsConstructor
public class ChatRoomUseCase {
    private final ChatRoomSaveService chatRoomSaveService;
    private final ChatRoomSearchService chatRoomSearchService;

    private final ChatMemberSearchService chatMemberSearchService;

    public ChatRoomRes.Detail createChatRoom(ChatRoomReq.Create request, Long userId) {
        ChatRoom chatRoom = chatRoomSaveService.createChatRoom(request, userId);

        return ChatRoomMapper.toChatRoomResDetail(chatRoom, true, 1);
    }

    public List<ChatRoomRes.Detail> getChatRooms(Long userId) {
        List<ChatRoomDetail> chatRooms = chatRoomSearchService.readChatRooms(userId);

        return ChatRoomMapper.toChatRoomResDetails(chatRooms);
    }

    public ChatRoomRes.Summary readJoinedChatRoomIds(Long userId) {
        Set<Long> chatRoomIds = chatMemberSearchService.readJoinedChatRoomIds(userId);

        return new ChatRoomRes.Summary(chatRoomIds);
    }

    public SliceResponseTemplate<ChatRoomRes.Detail> searchChatRooms(Long userId, String target, Pageable pageable) {
        Slice<ChatRoomDetail> chatRooms = chatRoomSearchService.readChatRoomsBySearch(userId, target, pageable);

        return ChatRoomMapper.toChatRoomResDetails(chatRooms, pageable);
    }
}
