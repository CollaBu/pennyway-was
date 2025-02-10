package kr.co.pennyway.api.apis.chat.usecase;

import kr.co.pennyway.api.apis.chat.dto.ChatRoomReq;
import kr.co.pennyway.api.apis.chat.dto.ChatRoomRes;
import kr.co.pennyway.api.apis.chat.mapper.ChatRoomMapper;
import kr.co.pennyway.api.apis.chat.service.ChatRoomPatchHelper;
import kr.co.pennyway.api.apis.chat.service.ChatRoomSaveService;
import kr.co.pennyway.api.apis.chat.service.ChatRoomSearchService;
import kr.co.pennyway.api.apis.chat.service.ChatRoomWithParticipantsSearchService;
import kr.co.pennyway.api.common.response.SliceResponseTemplate;
import kr.co.pennyway.api.common.storage.AwsS3Adapter;
import kr.co.pennyway.common.annotation.UseCase;
import kr.co.pennyway.domain.context.chat.dto.ChatRoomDeleteCommand;
import kr.co.pennyway.domain.context.chat.dto.ChatRoomToggleCommand;
import kr.co.pennyway.domain.context.chat.service.ChatMemberService;
import kr.co.pennyway.domain.context.chat.service.ChatRoomDeleteService;
import kr.co.pennyway.domain.context.chat.service.ChatRoomNotificationToggleService;
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
    private final ChatRoomWithParticipantsSearchService chatRoomWithParticipantsSearchService;
    private final ChatRoomPatchHelper chatRoomPatchHelper;
    private final ChatRoomDeleteService chatRoomDeleteService;
    private final ChatRoomNotificationToggleService chatRoomNotificationToggleService;

    private final ChatMemberService chatMemberService;

    private final AwsS3Adapter awsS3Adapter;

    public ChatRoomRes.Detail createChatRoom(ChatRoomReq.Create request, Long userId) {
        ChatRoom chatRoom = chatRoomSaveService.createChatRoom(request, userId);

        return ChatRoomMapper.toChatRoomResDetail(chatRoom, null, true, 1, 0, awsS3Adapter.getObjectPrefix());
    }

    public ChatRoomRes.AdminView getChatRoom(Long chatRoomId) {
        ChatRoom chatRoom = chatRoomSearchService.readChatRoom(chatRoomId);

        return ChatRoomMapper.toChatRoomResAdminView(chatRoom);
    }

    @Deprecated(since = "2025-01-22")
    public List<ChatRoomRes.Detail> getChatRooms(Long userId) {
        List<ChatRoomRes.Info> chatRooms = chatRoomSearchService.readChatRooms(userId);

        return ChatRoomMapper.toChatRoomResDetails(chatRooms, awsS3Adapter.getObjectPrefix());
    }

    public List<ChatRoomRes.Detailv2> getChatRoomDetails(Long userId) {
        List<ChatRoomRes.Info> chatRooms = chatRoomSearchService.readChatRooms(userId);

        return ChatRoomMapper.toChatRoomResDetailsV2(chatRooms, awsS3Adapter.getObjectPrefix());
    }

    public ChatRoomRes.RoomWithParticipants getChatRoomWithParticipants(Long userId, Long chatRoomId) {
        return chatRoomWithParticipantsSearchService.execute(userId, chatRoomId);
    }

    public ChatRoomRes.Summary readJoinedChatRoomIds(Long userId) {
        Set<Long> chatRoomIds = chatMemberService.readChatRoomIdsByUserId(userId);

        return new ChatRoomRes.Summary(chatRoomIds);
    }

    public SliceResponseTemplate<ChatRoomRes.Detail> searchChatRooms(Long userId, String target, Pageable pageable) {
        Slice<ChatRoomDetail> chatRooms = chatRoomSearchService.readChatRoomsBySearch(userId, target, pageable);

        return ChatRoomMapper.toChatRoomResDetails(chatRooms, pageable, awsS3Adapter.getObjectPrefix());
    }

    // 채팅방 자체의 정보 외엔 무의미한 데이터를 반환한다.
    public ChatRoomRes.Detail updateChatRoom(Long chatRoomId, ChatRoomReq.Update request) {
        ChatRoom chatRoom = chatRoomPatchHelper.updateChatRoom(chatRoomId, request);

        return ChatRoomMapper.toChatRoomResDetail(chatRoom, null, true, 1, 0, awsS3Adapter.getObjectPrefix());
    }

    public void turnOnNotification(Long userId, Long chatRoomId) {
        chatRoomNotificationToggleService.turnOn(ChatRoomToggleCommand.of(userId, chatRoomId));
    }

    public void turnOffNotification(Long userId, Long chatRoomId) {
        chatRoomNotificationToggleService.turnOff(ChatRoomToggleCommand.of(userId, chatRoomId));
    }

    public void deleteChatRoom(Long userId, Long chatRoomId) {
        chatRoomDeleteService.execute(ChatRoomDeleteCommand.of(userId, chatRoomId));
    }
}
