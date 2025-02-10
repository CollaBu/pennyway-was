package kr.co.pennyway.infra.client.aws.s3.url.properties;

import kr.co.pennyway.infra.client.aws.s3.ObjectKeyType;

import java.util.Map;
import java.util.Objects;

public final class ProfileUrlProperty extends BaseUrlProperty {
    private final Long userId;

    public ProfileUrlProperty(Long userId, String ext) {
        super(ext, ObjectKeyType.PROFILE);
        this.userId = Objects.requireNonNull(userId, "유저 아이디는 필수입니다.");
    }

    @Override
    public Map<String, String> variables() {
        return Map.of(
                "user_id", userId.toString(),
                "uuid", imageId,
                "timestamp", timestamp,
                "ext", ext
        );
    }
}
