package kr.co.pennyway.api.apis.question.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.co.pennyway.api.apis.question.dto.QuestionReq;
import kr.co.pennyway.api.config.ExternalApiIntegrationTest;
import kr.co.pennyway.domain.domains.question.domain.QuestionCategory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

//@WebMvcTest(controllers = QuestionController.class)
@ExternalApiIntegrationTest
@ActiveProfiles("local")
public class QuestionControllerTest {

    private final String expectedEmail = "test@gmail.com";
    private final String expectedContent = "test question content";
    private final QuestionCategory expectedCategory = QuestionCategory.ETC;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp(WebApplicationContext webApplicationContext) {
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .defaultRequest(post("/**"))//.with(csrf()))
                .build();
    }

    @Test
    @DisplayName("[1] 이메일, 내용을 필수로 입력해야 합니다.")
    void requiredInputError() throws Exception {
        // given
        QuestionReq request = new QuestionReq("", "", QuestionCategory.ETC);

        // when
        ResultActions resultActions = mockMvc.perform(
                post("/v1/questions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        );

        // then
        resultActions
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.fieldErrors.email").value("이메일을 입력해주세요"))
                .andExpect(jsonPath("$.fieldErrors.content").value("문의 내용을 입력해주세요"))
                .andDo(print());
    }

    @Test
    @DisplayName("[2] 이메일 형식 오류입니다.")
    void emailValidError() throws Exception {
        // given
        QuestionReq request = new QuestionReq("test", "test question content", QuestionCategory.ETC);

        // when
        ResultActions resultActions = mockMvc.perform(
                post("/v1/questions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        );

        // then
        resultActions
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.fieldErrors.email").value("이메일 형식이 올바르지 않습니다."))
                .andDo(print());
    }

    @Test
    @DisplayName(("[3] 문의 카테고리를 선택해주세요."))
    void categoryMissingError() throws Exception {
        // given
        QuestionReq request = new QuestionReq("team.collabu@gmail.com", "test question content", null);

        // when
        ResultActions resultActions = mockMvc.perform(
                post("/v1/questions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        );

        // then
        resultActions
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.fieldErrors.category").value("문의 카테고리를 입력해주세요"))
                .andDo(print());
    }

    @Test
    @DisplayName("[4] 정상적인 문의 요청입니다.")
    void sendQuestion() throws Exception {
        // given
        QuestionReq request = new QuestionReq(expectedEmail, expectedContent, expectedCategory);

        // when
        ResultActions resultActions = mockMvc.perform(
                post("/v1/questions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        );

        // then
        resultActions
                .andExpect(status().isOk())
                .andDo(print());
    }
}
