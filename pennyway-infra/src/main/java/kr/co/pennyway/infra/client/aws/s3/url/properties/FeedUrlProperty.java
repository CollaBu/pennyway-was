package kr.co.pennyway.infra.client.aws.s3.url.properties;

import kr.co.pennyway.common.util.UUIDUtil;
import kr.co.pennyway.infra.client.aws.s3.ObjectKeyType;

import java.util.Map;

public final class FeedUrlProperty extends BaseUrlProperty {
    private final String feedId;

    public FeedUrlProperty(String ext) {
        super(ext, ObjectKeyType.FEED);
        this.feedId = UUIDUtil.generateUUID();
    }

    @Override
    public Map<String, String> variables() {
        return Map.of(
                "feed_id", feedId,
                "uuid", imageId,
                "timestamp", timestamp,
                "ext", ext
        );
    }
}
