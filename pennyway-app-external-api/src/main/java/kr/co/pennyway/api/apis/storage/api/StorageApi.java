package kr.co.pennyway.api.apis.storage.api;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.co.pennyway.api.apis.storage.dto.PresignedUrlDto;

@Tag(name = "[S3 이미지 저장을 위한 Presigned URL 발급 API]")
public interface StorageApi {
	@Operation(summary = "S3 이미지 저장을 위한 Presigned URL 발급", description = "S3에 이미지를 저장하기 위한 Presigned URL을 발급합니다.")
	@Parameters({
			@Parameter(name = "type", description = "이미지 종류", required = true, in = ParameterIn.QUERY, examples = {
					@ExampleObject(value = "PROFILE"),
					@ExampleObject(value = "FEED"),
					@ExampleObject(value = "CHATROOM_PROFILE"),
					@ExampleObject(value = "CHAT"),
					@ExampleObject(value = "CHAT_PROFILE")
			}),
			@Parameter(name = "ext", description = "파일 확장자", required = true, examples = {
					@ExampleObject(value = "jpg"),
					@ExampleObject(value = "png"),
					@ExampleObject(value = "jpeg")
			}),
			@Parameter(name = "userId", description = "사용자 ID", example = "1"),
			@Parameter(name = "chatroomId", description = "채팅방 ID", example = "12345678-1234-5678-1234-567812345678"),
			@Parameter(name = "request", hidden = true)
	})
	@ApiResponses({
			@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = PresignedUrlDto.Res.class))),
			@ApiResponse(responseCode = "400", content = @Content(mediaType = "application/json", examples = {
					@ExampleObject(name = "필수 파라미터 누락", value = """
							    {
							        "code": "4001",
							        "message": "필수 파라미터가 누락되었습니다."
							    }
							""")
			})),
	})
	ResponseEntity<?> getPresignedUrl(@Validated PresignedUrlDto.Req req);
}
