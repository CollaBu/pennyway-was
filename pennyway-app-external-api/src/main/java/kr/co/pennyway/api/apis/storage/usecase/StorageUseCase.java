package kr.co.pennyway.api.apis.storage.usecase;

import kr.co.pennyway.api.apis.storage.dto.PresignedUrlDto;
import kr.co.pennyway.api.apis.storage.service.StorageService;
import kr.co.pennyway.common.annotation.UseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UseCase
@RequiredArgsConstructor
public class StorageUseCase {
	private final StorageService storageService;

	public PresignedUrlDto.PresignedUrlRes getPresignedUrl(PresignedUrlDto.PresignedUrlReq request) {
		String type = request.type();
		String ext = request.ext();
		String userId = request.userId();
		String chatroomId = request.chatroomId();
		return PresignedUrlDto.PresignedUrlRes.of(storageService.getPresignedUrl(type, ext, userId, chatroomId));
	}
}
