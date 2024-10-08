package kr.co.pennyway.api.apis.storage.usecase;

import kr.co.pennyway.api.apis.storage.dto.PresignedUrlDto;
import kr.co.pennyway.common.annotation.UseCase;
import kr.co.pennyway.infra.client.aws.s3.AwsS3Provider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UseCase
@RequiredArgsConstructor
public class StorageUseCase {
    private final AwsS3Provider awsS3Provider;

    public PresignedUrlDto.Res getPresignedUrl(Long userId, PresignedUrlDto.Req request) {
        return PresignedUrlDto.Res.of(
                awsS3Provider.generatedPresignedUrl(request.type(), request.ext(), userId.toString(), request.chatroomId())
        );
    }
}
