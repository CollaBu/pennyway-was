package kr.co.pennyway.api.apis.storage.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.co.pennyway.api.apis.storage.dto.PresignedUrlDto;
import kr.co.pennyway.api.common.annotation.ApiExceptionExplanation;
import kr.co.pennyway.api.common.annotation.ApiResponseExplanations;
import kr.co.pennyway.api.common.security.authentication.SecurityUserDetails;
import kr.co.pennyway.infra.common.exception.StorageErrorCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;

@Tag(name = "[S3 이미지 저장을 위한 Presigned URL 발급 API]")
public interface StorageApi {
    @Operation(summary = "S3 이미지 저장을 위한 Presigned URL 발급", description = "S3에 이미지를 저장하기 위한 Presigned URL을 발급합니다.")
    @Parameters({
            @Parameter(name = "type", description = "이미지 종류", required = true, in = ParameterIn.QUERY, examples = {
                    @ExampleObject(value = "PROFILE", name = "사용자 프로필"),
                    @ExampleObject(value = "FEED", name = "피드"),
                    @ExampleObject(value = "CHATROOM_PROFILE", name = "채팅방 프로필"),
                    @ExampleObject(value = "CHAT", name = "채팅"),
                    @ExampleObject(value = "CHAT_PROFILE", name = "채팅 프로필")
            }),
            @Parameter(name = "ext", description = "파일 확장자", required = true, in = ParameterIn.QUERY, examples = {
                    @ExampleObject(value = "jpg", name = "jpg"),
                    @ExampleObject(value = "png", name = "png"),
                    @ExampleObject(value = "jpeg", name = "jpeg")
            }),
            @Parameter(name = "chatroomId", description = "채팅방 ID", in = ParameterIn.QUERY, example = "123456789"),
            @Parameter(name = "request", hidden = true)
    })
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = PresignedUrlDto.Res.class)))
    @ApiResponseExplanations(errors = {
            @ApiExceptionExplanation(value = StorageErrorCode.class, constant = "NOT_FOUND", name = "요청한 리소스를 찾을 수 없음")
    })
    ResponseEntity<?> getPresignedUrl(@Validated PresignedUrlDto.Req req, BindingResult bindingResult, @AuthenticationPrincipal SecurityUserDetails user);
}
