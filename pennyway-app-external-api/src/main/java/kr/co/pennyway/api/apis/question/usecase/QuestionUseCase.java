package kr.co.pennyway.api.apis.question.usecase;

import jakarta.transaction.Transactional;
import kr.co.pennyway.api.apis.question.dto.QuestionReq;
import kr.co.pennyway.common.annotation.UseCase;
import kr.co.pennyway.domain.domains.question.domain.Question;
import kr.co.pennyway.domain.domains.question.service.QuestionService;
import kr.co.pennyway.infra.client.google.mail.GoogleMailSender;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UseCase
@AllArgsConstructor
public class QuestionUseCase {
    private final QuestionService questionService;
    private final GoogleMailSender googleMailSender;

    @Transactional
    public void sendQuestion(QuestionReq request) {
        Question question = request.toEntity();

        questionService.createQuestion(question);
        googleMailSender.sendMail(request.email(), request.content(), request.category().getTitle());
        // TODO : sendMail 메소드 이벤트 처리
    }
}
