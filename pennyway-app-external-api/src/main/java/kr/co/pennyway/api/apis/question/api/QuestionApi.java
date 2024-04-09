package kr.co.pennyway.api.apis.question.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.co.pennyway.api.apis.question.dto.QuestionReq;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "[문의 API]")
public interface QuestionApi {

    @Operation(summary = "문의 전송", description = "사용자는 관리자에게 문의 메일을 발송한다.")
    @Schema(name = "QuestionReqGeneral", title = "문의 요청", allowableValues = {"UTILIZATION", "BUG_REPORT", "SUGGESTION", "ETC"})
    ResponseEntity<?> sendQuestion(@RequestBody @Validated QuestionReq request);
}
