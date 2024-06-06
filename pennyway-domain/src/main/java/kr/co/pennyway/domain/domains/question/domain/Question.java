package kr.co.pennyway.domain.domains.question.domain;

import jakarta.persistence.*;
import kr.co.pennyway.domain.common.converter.QuestionCategoryConverter;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Getter
@Table(name = "Question")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String email;
    @Convert(converter = QuestionCategoryConverter.class)
    @Column(nullable = false)
    private QuestionCategory category;
    private String content;
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    private LocalDateTime deletedAt;

    @Builder
    private Question(String email, QuestionCategory category, String content) {
        if (!StringUtils.hasText(email)) {
            throw new IllegalArgumentException("email은 null이거나 빈 문자열이 될 수 없습니다.");
        } else if (!StringUtils.hasText(content)) {
            throw new IllegalArgumentException("content는 null이거나 빈 문자열이 될 수 없습니다.");
        }

        this.email = email;
        this.category = Objects.requireNonNull(category, "category는 null이 될 수 없습니다.");
        this.content = content;
    }
}
