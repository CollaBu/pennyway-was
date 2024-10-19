package kr.co.pennyway.infra.client.aws.s3;

import kr.co.pennyway.infra.client.aws.s3.url.generator.UrlGenerator;
import kr.co.pennyway.infra.client.aws.s3.url.properties.PresignedUrlPropertyFactory;
import kr.co.pennyway.infra.common.exception.StorageErrorCode;
import kr.co.pennyway.infra.common.exception.StorageException;
import kr.co.pennyway.infra.config.AwsS3Config;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

import java.net.URI;
import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class AwsS3Provider {
    private final AwsS3Config awsS3Config;
    private final S3Presigner s3Presigner;
    private final S3Client s3Client;

    /**
     * type에 해당하는 확장자를 가진 파일을 S3에 저장하기 위한 Presigned URL을 생성한다.
     *
     * @param factory {@link PresignedUrlPropertyFactory} : Presigned URL 생성을 위한 Property Factory
     * @return Presigned URL
     */
    public URI generatedPresignedUrl(PresignedUrlPropertyFactory factory) {
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(awsS3Config.getBucketName())
                    .key(generateObjectKey(factory))
                    .build();

            PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(r -> r.putObjectRequest(putObjectRequest)
                    .signatureDuration(Duration.ofMinutes(10)));

            return presignedRequest.url().toURI();
        } catch (Exception e) {
            log.error("Presigned URL 생성 중 오류 발생", e);
            throw new StorageException(StorageErrorCode.MISSING_REQUIRED_PARAMETER);
        }
    }

    /**
     * type에 해당하는 ObjectKeyTemplate을 적용하여 ObjectKey(S3에 저장하기 위한 정적 파일의 경로 및 이름)를 생성한다.
     *
     * @param factory {@link PresignedUrlPropertyFactory} : Presigned URL 생성을 위한 Property Factory
     * @return ObjectKey
     */
    private String generateObjectKey(PresignedUrlPropertyFactory factory) {
        return UrlGenerator.createDeleteUrl(factory.getProperty());
    }

    /**
     * S3에 파일이 존재하는지 확인한다.
     *
     * @param key : S3 버킷 내의 파일 키
     * @return 파일이 존재하면 true, 존재하지 않으면 false
     */
    public boolean isObjectExist(String key) {
        try {
            HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                    .bucket(awsS3Config.getBucketName())
                    .key(key)
                    .build();

            HeadObjectResponse headObjectResponse = s3Client.headObject(headObjectRequest);
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        } catch (Exception e) {
            log.error("파일 존재 여부 확인 중 오류 발생", e);
            throw new StorageException(StorageErrorCode.NOT_FOUND);
        }
    }

    /**
     * S3에 저장된 파일을 복사한다.
     *
     * @param type      : ObjectKeyType (PROFILE, FEED, CHATROOM_PROFILE, CHAT, CHAT_PROFILE)
     * @param sourceKey : 복사할 파일의 키
     * @return 복사된 파일의 키
     */
    public String copyObject(ObjectKeyType type, String sourceKey) {
        String originKey = UrlGenerator.convertDeleteToOriginUrl(type, sourceKey);

        try {
            CopyObjectRequest copyObjRequest = CopyObjectRequest.builder()
                    .sourceBucket(awsS3Config.getBucketName())
                    .sourceKey(sourceKey)
                    .destinationBucket(awsS3Config.getBucketName())
                    .destinationKey(originKey)
                    .storageClass(StorageClass.ONEZONE_IA)
                    .build();

            s3Client.copyObject(copyObjRequest);
        } catch (Exception e) {
            log.error("파일 복사 중 오류 발생", e);
            throw new StorageException(StorageErrorCode.INVALID_FILE);
        }

        return originKey;
    }

    public String getObjectPrefix() {
        return awsS3Config.getObjectPrefix();
    }

    public void deleteObject(String key) {
        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(awsS3Config.getBucketName())
                    .key(key)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
        } catch (Exception e) {
            log.error("파일 삭제 중 오류 발생", e);
            throw new StorageException(StorageErrorCode.INVALID_FILE);
        }
    }
}
