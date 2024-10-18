package kr.co.pennyway.infra.client.aws.s3.url.properties;

import kr.co.pennyway.infra.client.aws.s3.ObjectKeyType;

import java.util.Map;

public interface PresignedUrlProperty {
    String imageId();

    String timestamp();

    String ext();

    ObjectKeyType type();

    /**
     * Presigned URL 생성을 위한 변수들을 반환한다.
     * key는 각 변수명을 lowerCamelCase로 변환한 것이다.
     */
    Map<String, String> variables();
}
