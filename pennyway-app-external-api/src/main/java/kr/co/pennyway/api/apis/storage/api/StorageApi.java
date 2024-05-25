package kr.co.pennyway.api.apis.storage.api;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.co.pennyway.api.apis.storage.dto.PresignedUrlDto;

@Tag(name = "[S3 이미지 저장을 위한 Presigned URL 발급 API]")
public interface StorageApi {
	@Operation(summary = "S3 이미지 저장을 위한 Presigned URL 발급", description = "S3에 이미지를 저장하기 위한 Presigned URL을 발급합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", content = @Content(mediaType = "application/json", examples = {
					@ExampleObject(name = "Presigned URL 발급 성공", value = """
							{
								"code": "2000",
								"data": {
									"presignedUrl": "https://pennyway-s3-presigned-url.s3.ap-northeast-2.amazonaws.com/..."
								}
							}
							""")
			}))
	})
	ResponseEntity<?> getPresignedUrl(@Validated PresignedUrlDto.PresignedUrlReq req);
}
