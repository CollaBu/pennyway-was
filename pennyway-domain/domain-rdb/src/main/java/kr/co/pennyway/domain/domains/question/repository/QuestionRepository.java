package kr.co.pennyway.domain.domains.question.repository;

import kr.co.pennyway.domain.domains.question.domain.Question;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionRepository extends JpaRepository<Question, Long> {
}
