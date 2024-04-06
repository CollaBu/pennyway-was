package kr.co.pennyway.domain.domains.question.service;

import jakarta.transaction.Transactional;
import kr.co.pennyway.domain.domains.question.domain.Question;
import kr.co.pennyway.domain.domains.question.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class QuestionService {
    private final QuestionRepository questionRepository;

    @Transactional
    public Question createQuestion(Question question) { return questionRepository.save(question);}

}
