package kr.co.pennyway.domain.common.redis.message.domain;

import kr.co.pennyway.domain.common.redis.message.type.MessageCategoryType;
import kr.co.pennyway.domain.common.redis.message.type.MessageContentType;
import org.springframework.lang.NonNull;

import java.util.Objects;

/**
 * 채팅 메시지 생성을 위한 Step Builder입니다.
 * 필수 필드들을 순차적으로 설정하도록 강제하여 객체 생성의 안정성을 보장합니다.
 *
 * <p>사용 예시:
 * <pre>
 * ChatMessage message = ChatMessage.builder()
 *     .chatRoomId(123L)
 *     .chatId(456L)
 *     .content("Hello")
 *     .contentType(MessageContentType.TEXT)
 *     .categoryType(MessageCategoryType.NORMAL)
 *     .sender(789L)
 *     .build();
 * </pre>
 */
public final class ChatMessageBuilder {
    private Long chatRoomId;
    private Long chatId;
    private String content;
    private MessageContentType contentType;
    private MessageCategoryType categoryType;
    private long sender;

    private ChatMessageBuilder() {
    }

    /**
     * ChatMessage 빌더의 시작점입니다.
     *
     * @return 채팅방 ID 설정 단계
     */
    public static ChatRoomIdStep builder() {
        return new Steps();
    }

    Long getChatRoomId() {
        return chatRoomId;
    }

    Long getChatId() {
        return chatId;
    }

    String getContent() {
        return content;
    }

    MessageContentType getContentType() {
        return contentType;
    }

    MessageCategoryType getCategoryType() {
        return categoryType;
    }

    long getSender() {
        return sender;
    }

    /**
     * 채팅방 ID 설정 단계입니다.
     * 채팅 메시지가 속한 채팅방의 ID를 지정합니다.
     */
    public interface ChatRoomIdStep {
        /**
         * 채팅방 ID를 설정합니다.
         *
         * @param chatRoomId 채팅방 ID
         * @return 채팅 메시지 ID 설정 단계
         * @throws NullPointerException chatRoomId가 null인 경우
         */
        ChatIdStep chatRoomId(Long chatRoomId);
    }

    /**
     * 채팅 메시지 ID 설정 단계입니다.
     * 개별 채팅 메시지를 식별하기 위한 ID를 지정합니다.
     */
    public interface ChatIdStep {
        /**
         * 채팅 메시지 ID를 설정합니다.
         *
         * @param chatId 채팅 메시지 ID
         * @return 메시지 내용 설정 단계
         * @throws NullPointerException chatId가 null인 경우
         */
        ContentStep chatId(Long chatId);
    }

    /**
     * 메시지 내용 설정 단계입니다.
     * 채팅 메시지의 실제 내용을 지정합니다.
     */
    public interface ContentStep {
        /**
         * 메시지 내용을 설정합니다.
         *
         * @param content 메시지 내용
         * @return 메시지 타입 설정 단계
         * @throws NullPointerException     content가 null인 경우
         * @throws IllegalArgumentException content가 5000자를 초과하는 경우
         */
        ContentTypeStep content(String content);
    }

    /**
     * 메시지 타입 설정 단계입니다.
     * 메시지의 형식(텍스트, 이미지, 파일 등)을 지정합니다.
     */
    public interface ContentTypeStep {
        /**
         * 메시지 타입을 설정합니다.
         *
         * @param contentType 메시지 타입
         * @return 메시지 카테고리 설정 단계
         * @throws NullPointerException contentType이 null인 경우
         */
        CategoryTypeStep contentType(MessageContentType contentType);
    }

    /**
     * 메시지 카테고리 설정 단계입니다.
     * 메시지의 종류(일반, 시스템 등)를 지정합니다.
     */
    public interface CategoryTypeStep {
        /**
         * 메시지 카테고리를 설정합니다.
         *
         * @param categoryType 메시지 카테고리
         * @return 발신자 설정 단계
         * @throws NullPointerException categoryType이 null인 경우
         */
        SenderStep categoryType(MessageCategoryType categoryType);
    }

    /**
     * 발신자 설정 단계입니다.
     * 메시지를 보낸 사용자의 ID를 지정합니다.
     */
    public interface SenderStep {
        /**
         * 발신자 ID를 설정합니다.
         *
         * @param sender 발신자 ID
         * @return 빌드 단계
         */
        BuildStep sender(Long sender);
    }

    /**
     * 최종 빌드 단계입니다.
     * 모든 필수 필드가 설정된 후 ChatMessage 객체를 생성합니다.
     */
    public interface BuildStep {
        /**
         * 설정된 값들을 사용하여 ChatMessage 객체를 생성합니다.
         *
         * @return 생성된 ChatMessage 객체
         */
        ChatMessage build();
    }

    private static class Steps implements
            ChatRoomIdStep,
            ChatIdStep,
            ContentStep,
            ContentTypeStep,
            CategoryTypeStep,
            SenderStep,
            BuildStep {

        private final ChatMessageBuilder builder = new ChatMessageBuilder();

        @Override
        public ChatIdStep chatRoomId(@NonNull final Long chatRoomId) {
            builder.chatRoomId = Objects.requireNonNull(chatRoomId, "chatRoomId must not be null");
            return this;
        }

        @Override
        public ContentStep chatId(@NonNull final Long chatId) {
            builder.chatId = Objects.requireNonNull(chatId, "chatId must not be null");
            return this;
        }

        @Override
        public ContentTypeStep content(@NonNull final String content) {
            builder.content = Objects.requireNonNull(content, "content must not be null");

            if (content.length() > 5000) {
                throw new IllegalArgumentException("content length must be less than or equal to 5000");
            }

            return this;
        }

        @Override
        public CategoryTypeStep contentType(@NonNull final MessageContentType contentType) {
            builder.contentType = Objects.requireNonNull(contentType, "contentType must not be null");
            return this;
        }

        @Override
        public SenderStep categoryType(@NonNull final MessageCategoryType categoryType) {
            builder.categoryType = Objects.requireNonNull(categoryType, "categoryType must not be null");
            return this;
        }

        @Override
        public BuildStep sender(@NonNull final Long sender) {
            builder.sender = sender;
            return this;
        }

        @Override
        public ChatMessage build() {
            return new ChatMessage(builder);
        }
    }
}
