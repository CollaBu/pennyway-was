package kr.co.pennyway.api.apis.question.usecase;

import jakarta.transaction.Transactional;
import kr.co.pennyway.api.apis.question.dto.QuestionReq;
import kr.co.pennyway.common.annotation.UseCase;
import kr.co.pennyway.domain.context.support.service.QuestionService;
import kr.co.pennyway.domain.domains.question.domain.Question;
import kr.co.pennyway.infra.common.event.MailEvent;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;

@Slf4j
@UseCase
@AllArgsConstructor
public class QuestionUseCase {
    private final QuestionService questionService;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Transactional
    public void sendQuestion(QuestionReq request) {
        Question question = request.toEntity();

        questionService.createQuestion(question);
        applicationEventPublisher.publishEvent(MailEvent.of(request.email(), request.content(), request.category().getTitle()));
    }
}
