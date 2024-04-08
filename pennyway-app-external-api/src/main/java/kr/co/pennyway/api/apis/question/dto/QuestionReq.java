package kr.co.pennyway.api.apis.question.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import kr.co.pennyway.domain.domains.question.domain.Question;
import kr.co.pennyway.domain.domains.question.domain.QuestionCategory;

public class QuestionReq {
    @Schema(name = "QuestionReqGeneral", title = "문의 요청", allowableValues = {"UTILIZATION", "BUG_REPORT", "SUGGESTION", "ETC"})
    public record General(
            @Schema(description = "믄의자 이메일", example = "foobar@gmail.com")
            @NotBlank(message = "이메일을 입력해주세요")
            String email,
            @Schema(description = "문의 내용", example = "문의 내용입니다.")
            @NotBlank(message = "문의 내용을 입력해주세요")
            String content,
            @Schema(description = "문의 카테고리", example = "UTILIZATION")
            @NotBlank(message = "문의 카테고리를 입력해주세요")
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
}
