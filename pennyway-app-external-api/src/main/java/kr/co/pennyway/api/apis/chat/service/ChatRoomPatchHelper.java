package kr.co.pennyway.api.apis.chat.service;

import kr.co.pennyway.api.apis.chat.dto.ChatRoomReq;
import kr.co.pennyway.api.common.storage.AwsS3Adapter;
import kr.co.pennyway.common.annotation.Helper;
import kr.co.pennyway.domain.context.chat.dto.ChatRoomPatchCommand;
import kr.co.pennyway.domain.context.chat.service.ChatRoomPatchService;
import kr.co.pennyway.domain.domains.chatroom.domain.ChatRoom;
import kr.co.pennyway.infra.client.aws.s3.ActualIdProvider;
import lombok.RequiredArgsConstructor;

@Helper
@RequiredArgsConstructor
public class ChatRoomPatchHelper {
    private final ChatRoomPatchService chatRoomPatchService;
    private final AwsS3Adapter awsS3Adapter;

    public ChatRoom updateChatRoom(ChatRoomReq.Update request) {
        String originImageUrl = null;
        if (request.backgroundImageUrl() != null) {
            originImageUrl = awsS3Adapter.saveImage(request.backgroundImageUrl(), ActualIdProvider.createInstanceOfChatroomProfile(request.chatRoomId()));
        }

        Integer password = (request.password() == null) ? null : Integer.valueOf(request.password());

        return chatRoomPatchService.execute(ChatRoomPatchCommand.of(request.chatRoomId(), request.title(), request.description(), originImageUrl, password));
    }
}
