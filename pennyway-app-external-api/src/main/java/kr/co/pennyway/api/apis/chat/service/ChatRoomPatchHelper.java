package kr.co.pennyway.api.apis.chat.service;

import kr.co.pennyway.api.apis.chat.dto.ChatRoomReq;
import kr.co.pennyway.api.common.storage.AwsS3Adapter;
import kr.co.pennyway.common.annotation.Helper;
import kr.co.pennyway.domain.context.chat.dto.ChatRoomPatchCommand;
import kr.co.pennyway.domain.context.chat.service.ChatRoomPatchService;
import kr.co.pennyway.domain.context.chat.service.ChatRoomService;
import kr.co.pennyway.domain.domains.chatroom.domain.ChatRoom;
import kr.co.pennyway.domain.domains.chatroom.exception.ChatRoomErrorCode;
import kr.co.pennyway.domain.domains.chatroom.exception.ChatRoomErrorException;
import kr.co.pennyway.infra.client.aws.s3.ActualIdProvider;
import kr.co.pennyway.infra.common.exception.StorageErrorCode;
import kr.co.pennyway.infra.common.exception.StorageException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Helper
@RequiredArgsConstructor
public class ChatRoomPatchHelper {
    private final ChatRoomService chatRoomService;
    private final ChatRoomPatchService chatRoomPatchService;
    private final AwsS3Adapter awsS3Adapter;

    public ChatRoom updateChatRoom(Long chatRoomId, ChatRoomReq.Update request) {
        ChatRoom currentChatRoom = chatRoomService.readChatRoom(chatRoomId)
                .orElseThrow(() -> new ChatRoomErrorException(ChatRoomErrorCode.NOT_FOUND_CHAT_ROOM));

        String originImageUrl = updateImage(currentChatRoom.getBackgroundImageUrl(), request.backgroundImageUrl(), chatRoomId);

        Integer password = (request.password() == null) ? null : Integer.valueOf(request.password());

        return chatRoomPatchService.execute(ChatRoomPatchCommand.of(chatRoomId, request.title(), request.description(), originImageUrl, password));
    }

    /**
     * 현재 채팅방 이미지와 요청된 이미지를 비교하여, 변경이 필요한 경우 처리합니다.
     *
     * @param currentImageUrl String : 현재 이미지 URL
     * @param requestImageUrl String : 요청된 이미지 URL
     * @param chatRoomId      Long : 채팅방 ID
     * @return 채팅방에 저장될 이미지 URL
     */
    private String updateImage(String currentImageUrl, String requestImageUrl, Long chatRoomId) {
        if (currentImageUrl != null && shouldDeleteCurrentImage(requestImageUrl)) { // 현재 이미지가 있고, 다른 이미지(null 혹은 신규)로 변경하는 경우, 현재 이미지 삭제
            log.info("현재 이미지 삭제: {}", currentImageUrl);
            awsS3Adapter.deleteImage(currentImageUrl);
        }

        return processRequestImage(requestImageUrl, chatRoomId);
    }

    private boolean shouldDeleteCurrentImage(String requestImageUrl) {
        return requestImageUrl == null || requestImageUrl.startsWith("delete/");
    }

    private String processRequestImage(String requestImageUrl, Long chatRoomId) {
        if (requestImageUrl == null) {
            log.info("{}번 채팅방 신규 이미지 없음", chatRoomId);

            return null;
        }

        if (requestImageUrl.startsWith("delete/")) {
            log.info("{}번 채팅방 신규 이미지 등록 요청: {}", chatRoomId, requestImageUrl);

            return awsS3Adapter.saveImage(requestImageUrl, ActualIdProvider.createInstanceOfChatroomProfile(chatRoomId));
        }

        if (requestImageUrl.startsWith(awsS3Adapter.getObjectPrefix())) {
            validateExistingImage(requestImageUrl);

            return requestImageUrl.substring(awsS3Adapter.getObjectPrefix().length());
        }

        throw new StorageException(StorageErrorCode.INVALID_IMAGE_PATH);
    }

    private void validateExistingImage(String imageUrl) {
        if (!awsS3Adapter.isObjectExist(imageUrl)) {
            throw new StorageException(StorageErrorCode.NOT_FOUND);
        }
    }
}
