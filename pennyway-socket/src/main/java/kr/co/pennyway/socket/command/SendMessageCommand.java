package kr.co.pennyway.socket.command;

import kr.co.pennyway.domain.common.redis.message.type.MessageCategoryType;
import kr.co.pennyway.domain.common.redis.message.type.MessageContentType;
import kr.co.pennyway.socket.common.constants.SystemMessageConstants;

/**
 * 채팅 메시지 전송을 위한 Command 클래스
 */
public record SendMessageCommand(
        long chatRoomId,
        String content,
        MessageContentType contentType,
        MessageCategoryType categoryType,
        long senderId
) {
    public SendMessageCommand {
        if (chatRoomId <= 0) {
            throw new IllegalArgumentException("채팅방 아이디는 0 혹은 음수일 수 없습니다.");
        }
        if (content.length() > 5000) {
            throw new IllegalArgumentException("메시지 내용은 5000자를 초과할 수 없습니다.");
        }
        if (contentType == null) {
            throw new IllegalArgumentException("메시지 타입은 NULL일 수 없습니다.");
        }
        if (categoryType == null) {
            throw new IllegalArgumentException("메시지 카테고리는 NULL일 수 없습니다.");
        }
        if (senderId < 0) {
            throw new IllegalArgumentException("발신자 아이디는 음수일 수 없습니다.");
        }
    }

    /**
     * 시스템 메시지를 생성합니다.
     *
     * @param chatRoomId long : 채팅방 아이디
     * @param content    String : 메시지 내용
     * @return {@link MessageContentType#TEXT}, {@link MessageCategoryType#SYSTEM}, {@link SystemMessageConstants#SYSTEM_SENDER_ID}로 생성된 SendMessageCommand
     */
    public static SendMessageCommand createSystemMessage(long chatRoomId, String content) {
        return new SendMessageCommand(
                chatRoomId,
                content,
                MessageContentType.TEXT,
                MessageCategoryType.SYSTEM,
                SystemMessageConstants.SYSTEM_SENDER_ID
        );
    }

    /**
     * 사용자 메시지를 생성합니다.
     *
     * @param chatRoomId  long : 채팅방 아이디
     * @param content     String : 메시지 내용
     * @param contentType {@link MessageContentType} : 메시지 타입
     * @param senderId    long : 발신자 아이디
     * @return {@link MessageCategoryType#NORMAL}로 생성된 SendMessageCommand
     */
    public static SendMessageCommand createUserMessage(long chatRoomId, String content, MessageContentType contentType, long senderId) {
        return new SendMessageCommand(
                chatRoomId,
                content,
                contentType,
                MessageCategoryType.NORMAL,
                senderId
        );
    }
}