package kr.co.pennyway.api.apis.chat.service;

import kr.co.pennyway.api.apis.chat.dto.ChatRoomReq;
import kr.co.pennyway.api.common.storage.AwsS3Adapter;
import kr.co.pennyway.api.config.fixture.ChatRoomFixture;
import kr.co.pennyway.domain.context.chat.service.ChatRoomPatchService;
import kr.co.pennyway.domain.context.chat.service.ChatRoomService;
import kr.co.pennyway.domain.domains.chatroom.domain.ChatRoom;
import kr.co.pennyway.infra.common.exception.StorageException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatRoomPatchHelperTest {
    private static final Long CHATROOM_ID = 1L;
    private static final String DELETE_PATH = "delete/chatroom/1/test-uuid_123.jpg";
    private static final String CONVERTED_PATH = "chatroom/1/test-uuid_123.jpg";
    private static final String ORIGIN_PATH = "chatroom/1/origin/test-uuid_321.jpg";

    @Mock
    private ChatRoomService chatRoomService;
    @Mock
    private ChatRoomPatchService chatRoomPatchService;
    @Mock
    private AwsS3Adapter awsS3Adapter;
    @InjectMocks
    private ChatRoomPatchHelper chatRoomPatchHelper;

    @Test
    @DisplayName("이미지가 없는 채팅방에 이미지 추가")
    void addNewImageToEmptyRoom() {
        // given
        ChatRoom chatRoom = ChatRoomFixture.PUBLIC_CHAT_ROOM.toEntity(CHATROOM_ID);
        ReflectionTestUtils.setField(chatRoom, "backgroundImageUrl", null);

        when(chatRoomService.readChatRoom(CHATROOM_ID)).thenReturn(Optional.of(chatRoom));
        when(awsS3Adapter.saveImage(eq(DELETE_PATH), any())).thenReturn(CONVERTED_PATH);

        ChatRoomReq.Update request = new ChatRoomReq.Update("title", "desc", null, DELETE_PATH);

        // when
        chatRoomPatchHelper.updateChatRoom(CHATROOM_ID, request);

        // then
        verify(awsS3Adapter, never()).deleteImage(anyString());
        verify(awsS3Adapter).saveImage(eq(DELETE_PATH), any());
    }

    @Test
    @DisplayName("이미지가 있는 채팅방의 이미지 삭제")
    void deleteExistingImage() {
        // given
        ChatRoom chatRoom = ChatRoomFixture.PUBLIC_CHAT_ROOM.toEntity(CHATROOM_ID);
        ReflectionTestUtils.setField(chatRoom, "backgroundImageUrl", ORIGIN_PATH);

        when(chatRoomService.readChatRoom(CHATROOM_ID)).thenReturn(Optional.of(chatRoom));

        ChatRoomReq.Update request = new ChatRoomReq.Update("title", "desc", null, null);

        // when
        chatRoomPatchHelper.updateChatRoom(CHATROOM_ID, request);

        // then
        verify(awsS3Adapter).deleteImage(ORIGIN_PATH);
        verify(awsS3Adapter, never()).saveImage(anyString(), any());
    }

    @Test
    @DisplayName("이미지가 있는 채팅방의 이미지 변경")
    void updateExistingImage() {
        // given
        ChatRoom chatRoom = ChatRoomFixture.PUBLIC_CHAT_ROOM.toEntity(CHATROOM_ID);
        ReflectionTestUtils.setField(chatRoom, "backgroundImageUrl", ORIGIN_PATH);

        when(chatRoomService.readChatRoom(CHATROOM_ID)).thenReturn(Optional.of(chatRoom));
        when(awsS3Adapter.saveImage(eq(DELETE_PATH), any())).thenReturn(CONVERTED_PATH);

        ChatRoomReq.Update request = new ChatRoomReq.Update("title", "desc", null, DELETE_PATH);

        // when
        chatRoomPatchHelper.updateChatRoom(CHATROOM_ID, request);

        // then
        verify(awsS3Adapter).deleteImage(ORIGIN_PATH);
        verify(awsS3Adapter).saveImage(eq(DELETE_PATH), any());
    }

    @Test
    @DisplayName("이미지가 있는 채팅방의 이미지 유지")
    void keepExistingImage() {
        // given
        ChatRoom chatRoom = ChatRoomFixture.PUBLIC_CHAT_ROOM.toEntity(CHATROOM_ID);
        ReflectionTestUtils.setField(chatRoom, "backgroundImageUrl", ORIGIN_PATH);

        when(chatRoomService.readChatRoom(CHATROOM_ID)).thenReturn(Optional.of(chatRoom));
        when(awsS3Adapter.getObjectPrefix()).thenReturn("https://cdn.test.com/");
        when(awsS3Adapter.isObjectExist("https://cdn.test.com/" + ORIGIN_PATH)).thenReturn(true);

        ChatRoomReq.Update request = new ChatRoomReq.Update("title", "desc", null, "https://cdn.test.com/" + ORIGIN_PATH);

        // when
        chatRoomPatchHelper.updateChatRoom(CHATROOM_ID, request);

        // then
        verify(awsS3Adapter, never()).deleteImage(anyString());
        verify(awsS3Adapter, never()).saveImage(anyString(), any());
        verify(awsS3Adapter).isObjectExist("https://cdn.test.com/" + ORIGIN_PATH);
    }

    @Test
    @DisplayName("잘못된 이미지 URL 패턴으로 요청시 예외 발생")
    void throwExceptionForInvalidUrlPattern() {
        // given
        ChatRoom chatRoom = ChatRoomFixture.PUBLIC_CHAT_ROOM.toEntity(CHATROOM_ID);
        ReflectionTestUtils.setField(chatRoom, "backgroundImageUrl", ORIGIN_PATH);

        when(chatRoomService.readChatRoom(CHATROOM_ID)).thenReturn(Optional.of(chatRoom));
        when(awsS3Adapter.getObjectPrefix()).thenReturn("https://cdn.test.com/");

        ChatRoomReq.Update request = new ChatRoomReq.Update("title", "desc", null, "invalid/path/image.jpg");

        // when & then
        assertThrows(StorageException.class, () -> chatRoomPatchHelper.updateChatRoom(CHATROOM_ID, request));
    }
}