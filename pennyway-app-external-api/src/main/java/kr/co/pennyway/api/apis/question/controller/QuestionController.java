package kr.co.pennyway.api.apis.question.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.co.pennyway.api.apis.question.dto.QuestionReq;
import kr.co.pennyway.api.apis.question.usecase.QuestionUseCase;
import kr.co.pennyway.api.common.response.SuccessResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Tag(name = "[문의 API]")
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/question")
public class QuestionController {
    private final QuestionUseCase questionUseCase;

    @Operation(summary = "문의 전송")
    @GetMapping("/send")
    @PreAuthorize("isAnonymous()")
    public ResponseEntity<?> sendQuestion(@RequestBody @Validated QuestionReq.General request){
        questionUseCase.sendQuestion(request);
        return ResponseEntity.ok(SuccessResponse.noContent());
    }
}
