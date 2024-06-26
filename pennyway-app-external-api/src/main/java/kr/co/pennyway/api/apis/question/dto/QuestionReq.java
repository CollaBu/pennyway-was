package kr.co.pennyway.api.apis.question.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import kr.co.pennyway.domain.domains.question.domain.Question;
import kr.co.pennyway.domain.domains.question.domain.QuestionCategory;

public record QuestionReq(
        @Schema(description = "문의자 이메일", example = "foobar@gmail.com")
        @NotBlank(message = "이메일을 입력해주세요")
        @Email(message = "이메일 형식이 올바르지 않습니다.")
        String email,
        @Schema(description = "문의 내용", example = "문의 내용입니다.")
        @Size(min = 1, max = 5000, message = "문의 내용은 1자 이상 5000자 이하로 입력해주세요")
        String content,
        @Schema(description = "문의 카테고리", example = "UTILIZATION")
        @NotNull(message = "문의 카테고리를 입력해주세요")
        QuestionCategory category
) {
    public Question toEntity() {
        return Question.builder()
                .email(email)
                .content(content)
                .category(category)
                .build();
    }
}