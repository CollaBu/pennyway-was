package kr.co.pennyway.api.apis.question.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.co.pennyway.domain.domains.question.domain.Question;
import kr.co.pennyway.domain.domains.question.domain.QuestionCategory;

public class QuestionReq {
    @Schema(name = "QuestionReqGeneral", title = "문의 요청", allowableValues = {"UTILIZATION", "BUG_REPORT", "SUGGESTION", "ETC"})
    public record General(
            String email,
            String content,
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
