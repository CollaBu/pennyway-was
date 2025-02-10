package kr.co.pennyway.api.common.storage;

import kr.co.pennyway.common.annotation.Adapter;
import kr.co.pennyway.infra.client.aws.s3.ActualIdProvider;
import kr.co.pennyway.infra.client.aws.s3.AwsS3Provider;
import kr.co.pennyway.infra.common.exception.StorageErrorCode;
import kr.co.pennyway.infra.common.exception.StorageException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Adapter
@RequiredArgsConstructor
public class AwsS3Adapter {
    private final AwsS3Provider awsS3Provider;

    /**
     * 임시 저장 경로에서 원본 저장 경로로 사진을 복사하고, 원본이 저장된 키를 반환합니다.
     *
     * @param deleteImageUrl String : 임시 저장 이미지 URL
     * @param type           {@link ActualIdProvider} : 실제 ID를 제공하는 클래스
     * @return 프로필 이미지 원본이 저장된 key
     * @throws StorageException 프로필 이미지 URL이 유효하지 않을 때
     */
    public String saveImage(String deleteImageUrl, ActualIdProvider type) {
        if (!awsS3Provider.isObjectExist(deleteImageUrl)) {
            log.info("프로필 이미지 URL이 유효하지 않습니다.");
            throw new StorageException(StorageErrorCode.NOT_FOUND);
        }

        return awsS3Provider.copyObject(type, deleteImageUrl);
    }

    /**
     * 프로필 이미지를 삭제합니다.
     *
     * @param key 프로필 이미지 key
     * @throws StorageException 프로필 이미지 URL이 유효하지 않을 때
     */
    public void deleteImage(String key) {
        if (!awsS3Provider.isObjectExist(key)) {
            log.info("프로필 이미지 URL이 유효하지 않습니다.");
            throw new StorageException(StorageErrorCode.NOT_FOUND);
        }

        awsS3Provider.deleteObject(key);
    }

    /**
     * Image URL에 해당하는 Object가 존재하는지 확인합니다.
     */
    public boolean isObjectExist(String imageUrl) {
        return awsS3Provider.isObjectExist(imageUrl);
    }

    /**
     * S3에 저장된 Object의 Prefix를 반환합니다.
     */
    public String getObjectPrefix() {
        return awsS3Provider.getObjectPrefix();
    }
}
