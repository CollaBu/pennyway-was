package kr.co.pennyway.domain.domains.question.domain;

import jakarta.persistence.*;
import kr.co.pennyway.domain.domains.user.domain.User;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "Question")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;

    private QuestionCategory category;
    private String content;


    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ColumnDefault("NULL")
    private LocalDateTime deletedAt;

    @Builder
    private Question(String email, QuestionCategory category, String content, LocalDateTime createdAt, LocalDateTime deletedAt) {
        this.email = email;
        this.category = category;
        this.content = content;
        this.createdAt = createdAt;
        this.deletedAt = deletedAt;
    }
}
