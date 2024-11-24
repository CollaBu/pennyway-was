package kr.co.pennyway.domain.context.support.service;

import kr.co.pennyway.common.annotation.DomainService;
import kr.co.pennyway.domain.domains.question.domain.Question;
import kr.co.pennyway.domain.domains.question.service.QuestionRdbService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@DomainService
@RequiredArgsConstructor
public class QuestionService {
    private final QuestionRdbService questionRdbService;

    @Transactional
    public void createQuestion(Question question) {
        questionRdbService.createQuestion(question);
    }
}
