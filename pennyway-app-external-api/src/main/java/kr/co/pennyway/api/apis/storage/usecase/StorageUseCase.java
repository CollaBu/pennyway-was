package kr.co.pennyway.api.apis.storage.usecase;

import kr.co.pennyway.api.apis.storage.adapter.PresignedUrlGenerateAdapter;
import kr.co.pennyway.api.apis.storage.dto.PresignedUrlDto;
import kr.co.pennyway.common.annotation.UseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UseCase
@RequiredArgsConstructor
public class StorageUseCase {
    private final PresignedUrlGenerateAdapter presignedUrlGenerateAdapter;

    public PresignedUrlDto.Res getPresignedUrl(Long userId, PresignedUrlDto.Req request) {
        return PresignedUrlDto.Res.of(presignedUrlGenerateAdapter.execute(userId, request));
    }
}
