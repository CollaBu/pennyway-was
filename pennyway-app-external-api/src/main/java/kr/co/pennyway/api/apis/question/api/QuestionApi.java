package kr.co.pennyway.api.apis.question.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.co.pennyway.api.apis.question.dto.QuestionReq;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "[문의 API]")
public interface QuestionApi {

    @Operation(summary = "문의 전송", description = "사용자는 관리자에게 문의 메일을 발송한다.")
    @ApiResponse(responseCode = "200", content = @Content(mediaType = "application/json", examples = {
            @ExampleObject(name = "발신 성공", value = """
                    {
                        "code": "2000",
                        "data": {}
                    }
                    """)
    }))
    ResponseEntity<?> sendQuestion(@RequestBody @Validated QuestionReq request);
}
