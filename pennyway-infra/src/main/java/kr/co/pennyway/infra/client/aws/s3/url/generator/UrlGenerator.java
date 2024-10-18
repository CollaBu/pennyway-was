package kr.co.pennyway.infra.client.aws.s3.url.generator;

import kr.co.pennyway.infra.client.aws.s3.url.properties.PresignedUrlProperty;

import java.util.Map;

public final class UrlGenerator {
    /**
     * S3에 임시 업로드할 파일의 URL을 생성한다.
     *
     * @param property {@link PresignedUrlProperty}: Presigned URL 생성을 위한 Property
     * @return Presigned URL
     */
    public static String createDeleteUrl(PresignedUrlProperty property) {
        return applyTemplate(property.type().getDeleteTemplate(), property.variables());
    }

    /**
     * 임시 경로에서 실제 경로로 파일을 이동시키기 위한 URL을 생성한다.
     *
     * @param property {@link PresignedUrlProperty}: Presigned URL 생성을 위한 Property
     * @return Presigned URL
     */
    public static String createOriginUrl(PresignedUrlProperty property) {
        return applyTemplate(property.type().getOriginTemplate(), property.variables());
    }

    private static String applyTemplate(String template, Map<String, String> variables) {
        String result = template;
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            result = result.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return result;
    }
}
