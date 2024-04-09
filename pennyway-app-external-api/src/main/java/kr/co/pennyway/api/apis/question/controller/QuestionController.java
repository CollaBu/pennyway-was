package kr.co.pennyway.api.apis.question.controller;

import kr.co.pennyway.api.apis.question.api.QuestionApi;
import kr.co.pennyway.api.apis.question.dto.QuestionReq;
import kr.co.pennyway.api.apis.question.usecase.QuestionUseCase;
import kr.co.pennyway.api.common.response.SuccessResponse;
import kr.co.pennyway.domain.domains.question.domain.Question;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/questions")
public class QuestionController implements QuestionApi {
    private final QuestionUseCase questionUseCase;

    @Override
    @PostMapping("")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> sendQuestion(@RequestBody @Validated QuestionReq request) {
        Question question = questionUseCase.sendQuestion(request);
        return ResponseEntity.ok(SuccessResponse.from(question));
    }
}
