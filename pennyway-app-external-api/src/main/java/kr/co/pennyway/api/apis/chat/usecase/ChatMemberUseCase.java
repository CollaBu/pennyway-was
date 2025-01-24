package kr.co.pennyway.api.apis.chat.usecase;

import kr.co.pennyway.api.apis.chat.dto.ChatMemberRes;
import kr.co.pennyway.api.apis.chat.dto.ChatRoomRes;
import kr.co.pennyway.api.apis.chat.mapper.ChatMemberMapper;
import kr.co.pennyway.api.apis.chat.mapper.ChatRoomMapper;
import kr.co.pennyway.api.apis.chat.service.ChatMemberJoinService;
import kr.co.pennyway.api.common.storage.AwsS3Adapter;
import kr.co.pennyway.common.annotation.UseCase;
import kr.co.pennyway.domain.context.chat.dto.ChatMemberBanCommand;
import kr.co.pennyway.domain.context.chat.dto.ChatRoomAdminDelegateCommand;
import kr.co.pennyway.domain.context.chat.service.ChatMemberBanService;
import kr.co.pennyway.domain.context.chat.service.ChatMemberService;
import kr.co.pennyway.domain.context.chat.service.ChatRoomAdminDelegateService;
import kr.co.pennyway.domain.context.chat.service.ChatRoomLeaveService;
import kr.co.pennyway.domain.domains.chatroom.domain.ChatRoom;
import kr.co.pennyway.domain.domains.member.dto.ChatMemberResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Triple;

import java.util.List;
import java.util.Set;

@Slf4j
@UseCase
@RequiredArgsConstructor
public class ChatMemberUseCase {
    private final ChatMemberJoinService chatMemberJoinService;
    private final ChatRoomLeaveService chatRoomLeaveService;
    private final ChatMemberBanService chatMemberBanService;
    private final ChatRoomAdminDelegateService chatRoomAdminDelegateService;

    private final ChatMemberService chatMemberService;
    private final AwsS3Adapter awsS3Adapter;

    public ChatRoomRes.Detail joinChatRoom(Long userId, Long chatRoomId, Integer password) {
        Triple<ChatRoom, Integer, Long> chatRoom = chatMemberJoinService.execute(userId, chatRoomId, password);

        return ChatRoomMapper.toChatRoomResDetail(chatRoom.getLeft(), null, false, chatRoom.getMiddle(), chatRoom.getRight(), awsS3Adapter.getObjectPrefix());
    }

    public List<ChatMemberRes.MemberDetail> readChatMembers(Long chatRoomId, Set<Long> chatMemberIds) {
        List<ChatMemberResult.Detail> chatMembers = chatMemberService.readChatMembersByMemberIds(chatRoomId, chatMemberIds);

        return ChatMemberMapper.toChatMemberResDetail(chatMembers, awsS3Adapter.getObjectPrefix());
    }

    public void leaveChatRoom(Long userId, Long chatRoomId) {
        chatRoomLeaveService.execute(userId, chatRoomId);
    }

    public void banChatMember(Long userId, Long targetMemberId, Long chatRoomId) {
        chatMemberBanService.execute(ChatMemberBanCommand.of(userId, targetMemberId, chatRoomId));
    }

    public void delegate(Long userId, Long targetChatMemberId, Long chatRoomId) {
        chatRoomAdminDelegateService.execute(ChatRoomAdminDelegateCommand.of(chatRoomId, userId, targetChatMemberId));
    }
}
