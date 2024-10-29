package kr.co.pennyway.api.apis.chat.usecase;

import kr.co.pennyway.api.apis.chat.dto.ChatRoomRes;
import kr.co.pennyway.api.apis.chat.mapper.ChatRoomMapper;
import kr.co.pennyway.api.apis.chat.service.ChatMemberJoinService;
import kr.co.pennyway.common.annotation.UseCase;
import kr.co.pennyway.domain.domains.chatroom.domain.ChatRoom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;

@Slf4j
@UseCase
@RequiredArgsConstructor
public class ChatMemberUseCase {
    private final ChatMemberJoinService chatMemberJoinService;

    public ChatRoomRes.Detail joinChatRoom(Long userId, Long chatRoomId, Integer password) {
        Pair<ChatRoom, Integer> chatRoom = chatMemberJoinService.execute(userId, chatRoomId, password);

        return ChatRoomMapper.toChatRoomResDetail(chatRoom.getLeft(), false, chatRoom.getRight());
    }
}
