package kr.co.pennyway.infra.client.aws.s3.url.generator;

import kr.co.pennyway.infra.client.aws.s3.ObjectKeyType;
import kr.co.pennyway.infra.common.exception.StorageErrorCode;
import kr.co.pennyway.infra.common.exception.StorageException;

public class UrlGeneratorFactory {
    public static UrlGenerator getUrlGenerator(ObjectKeyType type) {
        switch (type) {
            case PROFILE:
                return new ProfileUrlGenerator();
            case FEED:
                return new FeedUrlGenerator();
            case CHATROOM_PROFILE:
                return new ChatroomProfileUrlGenerator();
            case CHAT:
                return new ChatUrlGenerator();
            case CHAT_PROFILE:
                return new ChatProfileUrlGenerator();
            default:
                throw new StorageException(StorageErrorCode.INVALID_TYPE);
        }
    }
}
