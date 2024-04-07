package kr.co.pennyway.api.apis.question.api;

import io.lettuce.core.dynamic.annotation.Param;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.co.pennyway.api.apis.question.dto.QuestionReq;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "[문의 API]")
public interface QuestionApi {

    @Operation(summary = "문의 전송", description = "사용자는 관리자에게 문의 메일을 발송한다.")
    @Parameter(name = "email", description = "문의자 이메일")
    @Parameter(name = "content", description = "문의 내용")
    @Parameter(name = "category", description = "문의 카테고리" , examples = {
            @ExampleObject(name = "이용 관련", value = "UTILIZATION"), @ExampleObject(name = "오류 신고", value = "BUG_REPORT"), @ExampleObject(name = "서비스 제안", value = "SUGGESTION"), @ExampleObject(name = "기타", value = "ETC")
    }, required = true, in = ParameterIn.QUERY)

    ResponseEntity<?> sendQuestion(@RequestBody @Validated QuestionReq.General request);
}
