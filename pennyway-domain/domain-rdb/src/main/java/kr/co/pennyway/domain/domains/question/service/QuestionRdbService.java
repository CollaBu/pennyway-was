package kr.co.pennyway.domain.domains.question.service;

import kr.co.pennyway.common.annotation.DomainService;
import kr.co.pennyway.domain.domains.question.domain.Question;
import kr.co.pennyway.domain.domains.question.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@DomainService
@RequiredArgsConstructor
public class QuestionRdbService {
    private final QuestionRepository questionRepository;

    @Transactional
    public void createQuestion(Question question) {
        questionRepository.save(question);
    }

}
