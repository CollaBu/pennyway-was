package kr.co.pennyway.infra.client.aws.s3.url.generator;

import kr.co.pennyway.infra.client.aws.s3.ObjectKeyTemplate;
import kr.co.pennyway.infra.client.aws.s3.url.properties.PresignedUrlProperty;

public final class UrlGenerator {
    /**
     * S3에 임시 업로드할 파일의 URL을 생성한다.
     *
     * @param property {@link PresignedUrlProperty}: Presigned URL 생성을 위한 Property
     * @return Presigned URL
     */
    public static String createDeleteUrl(PresignedUrlProperty property) {
        return ObjectKeyTemplate.apply(property.type().getDeleteTemplate(), property.variables());
    }

    /**
     * 임시 경로에서 실제 경로로 파일을 이동시키기 위한 URL을 생성한다.
     *
     * @param property {@link PresignedUrlProperty}: Presigned URL 생성을 위한 Property
     * @return Presigned URL
     */
    public static String createOriginUrl(PresignedUrlProperty property) {
        return ObjectKeyTemplate.apply(property.type().getOriginTemplate(), property.variables());
    }
}
