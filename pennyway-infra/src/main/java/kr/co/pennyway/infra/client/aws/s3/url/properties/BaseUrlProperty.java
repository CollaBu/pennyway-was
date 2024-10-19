package kr.co.pennyway.infra.client.aws.s3.url.properties;

import kr.co.pennyway.common.util.UUIDUtil;
import kr.co.pennyway.infra.client.aws.s3.ObjectKeyType;

import java.util.Set;

public abstract class BaseUrlProperty implements PresignedUrlProperty {
    private static final Set<String> extensionSet = Set.of("jpg", "png", "jpeg");

    protected final String imageId;
    protected final String timestamp;
    protected final String ext;
    protected final ObjectKeyType type;

    protected BaseUrlProperty(String ext, ObjectKeyType type) {
        if (!extensionSet.contains(ext)) {
            throw new IllegalArgumentException("지원하지 않는 확장자입니다.");
        }

        this.imageId = UUIDUtil.generateUUID();
        this.timestamp = String.valueOf(System.currentTimeMillis());
        this.ext = ext;
        this.type = type;
    }

    @Override
    public String imageId() {
        return imageId;
    }

    @Override
    public String timestamp() {
        return timestamp;
    }

    @Override
    public String ext() {
        return ext;
    }

    @Override
    public ObjectKeyType type() {
        return type;
    }
}
