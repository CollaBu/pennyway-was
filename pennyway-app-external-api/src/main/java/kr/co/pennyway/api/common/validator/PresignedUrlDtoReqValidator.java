package kr.co.pennyway.api.common.validator;

import kr.co.pennyway.api.apis.storage.dto.PresignedUrlDto;
import kr.co.pennyway.infra.client.aws.s3.ObjectKeyType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * {@link kr.co.pennyway.api.apis.storage.dto.PresignedUrlDto.Req}의 유효성 검사를 담당하는 Validator
 */
@Slf4j
public class PresignedUrlDtoReqValidator implements Validator {
    @Override
    public boolean supports(@NonNull Class<?> clazz) {
        return PresignedUrlDto.Req.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(@NonNull Object target, @NonNull Errors errors) {
        PresignedUrlDto.Req req = (PresignedUrlDto.Req) target;

        if (ObjectKeyType.CHATROOM_PROFILE.equals(req.type()) && req.chatroomId() == null) {
            errors.rejectValue("chatroomId", "MISSING_CHATROOM_PARAMETER", "채팅방 이미지를 위해 채팅방 ID는 필수입니다.");
        }
        if (ObjectKeyType.CHAT_PROFILE.equals(req.type()) && req.chatroomId() == null) {
            errors.rejectValue("chatroomId", "MISSING_CHAT_PROFILE_PARAMETER", "채팅 프로필 이미지를 위해 채팅방 ID는 필수입니다.");
        }
        if (ObjectKeyType.CHAT.equals(req.type())) {
            if (req.chatroomId() == null) {
                errors.rejectValue("chatroomId", "MISSING_CHAT_PARAMETER", "채팅 이미지를 위해 채팅방 ID는 필수입니다.");
            }
            if (req.chatId() == null) {
                errors.rejectValue("chatId", "MISSING_CHAT_PARAMETER", "채팅 이미지를 위해 채팅 ID는 필수입니다.");
            }
        }
        if (ObjectKeyType.FEED.equals(req.type()) && req.feedId() == null) {
            errors.rejectValue("feedId", "MISSING_FEED_PARAMETER", "피드 이미지를 위해 피드 ID는 필수입니다.");
        }
    }
}
