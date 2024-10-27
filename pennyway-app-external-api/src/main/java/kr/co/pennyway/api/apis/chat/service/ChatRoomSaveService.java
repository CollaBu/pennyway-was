package kr.co.pennyway.api.apis.chat.service;

import kr.co.pennyway.api.apis.chat.dto.ChatRoomReq;
import kr.co.pennyway.api.common.storage.AwsS3Adapter;
import kr.co.pennyway.domain.domains.chatroom.domain.ChatRoom;
import kr.co.pennyway.domain.domains.chatroom.service.ChatRoomService;
import kr.co.pennyway.domain.domains.member.service.ChatMemberService;
import kr.co.pennyway.domain.domains.user.domain.User;
import kr.co.pennyway.domain.domains.user.exception.UserErrorCode;
import kr.co.pennyway.domain.domains.user.exception.UserErrorException;
import kr.co.pennyway.domain.domains.user.service.UserService;
import kr.co.pennyway.infra.client.aws.s3.ActualIdProvider;
import kr.co.pennyway.infra.client.guid.IdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatRoomSaveService {
    private final UserService userService;
    private final ChatRoomService chatRoomService;
    private final ChatMemberService chatMemberService;

    private final AwsS3Adapter awsS3Adapter;
    private final IdGenerator<Long> idGenerator;

    @Transactional
    public ChatRoom createChatRoom(ChatRoomReq.Create request, Long userId) {
        Long chatRoomId = idGenerator.generate();

        String originImageUrl = null;
        if (request.backgroundImageUrl() != null) {
            originImageUrl = awsS3Adapter.saveImage(request.backgroundImageUrl(), ActualIdProvider.createInstanceOfChatroomProfile(chatRoomId));
        }
        ChatRoom chatRoom = chatRoomService.create(request.toEntity(chatRoomId, originImageUrl));

        User user = userService.readUser(userId).orElseThrow(() -> new UserErrorException(UserErrorCode.NOT_FOUND));
        chatMemberService.createAdmin(user.getName(), user, chatRoom);

        return chatRoom;
    }
}
