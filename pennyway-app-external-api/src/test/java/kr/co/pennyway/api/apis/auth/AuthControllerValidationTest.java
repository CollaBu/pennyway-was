package kr.co.pennyway.api.apis.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.co.pennyway.api.apis.auth.controller.AuthController;
import kr.co.pennyway.api.apis.auth.dto.SignUpReq;
import kr.co.pennyway.api.apis.auth.usecase.AuthUseCase;
import kr.co.pennyway.api.common.security.jwt.Jwts;
import kr.co.pennyway.api.common.util.CookieUtil;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.Duration;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = {AuthController.class})
@ActiveProfiles("local")
public class AuthControllerValidationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthUseCase authUseCase;

    @MockBean
    private CookieUtil cookieUtil;

    @DisplayName("[1] 아이디, 이름, 비밀번호, 전화번호, 인증번호 필수 입력")
    @Test
    void requiredInputError() throws Exception {
        // given
        SignUpReq.General request = new SignUpReq.General("", "", "", "", "");

        // when
        ResultActions resultActions = mockMvc.perform(
                post("/api/v1/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        );

        // then
        resultActions
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.fieldErrors.username").exists())
                .andExpect(jsonPath("$.fieldErrors.name").exists())
                .andExpect(jsonPath("$.fieldErrors.password").exists())
                .andExpect(jsonPath("$.fieldErrors.phone").exists())
                .andExpect(jsonPath("$.fieldErrors.code").exists())
                .andDo(print());
    }

    @DisplayName("[2] 아이디는 5~20자의 영문 소문자, -, _, . 만 사용 가능합니다.")
    @Test
    void idValidError() throws Exception {
        // given
        SignUpReq.General request = new SignUpReq.General("#pennyway", "페니웨이", "pennyway1234", "010-1234-5678", "123456");

        // when
        ResultActions resultActions = mockMvc.perform(
                post("/api/v1/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        );

        // then
        resultActions
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.fieldErrors.username").value("5~20자의 영문 소문자, -, _, . 만 사용 가능합니다."))
                .andDo(print());
    }

    @DisplayName("[3] 이름은 2~20자의 한글, 영문 대/소문자만 사용 가능합니다.")
    @Test
    void nameValidError() throws Exception {
        // given
        SignUpReq.General request = new SignUpReq.General("pennyway", "페니웨이1", "pennyway1234", "010-1234-5678", "123456");

        // when
        ResultActions resultActions = mockMvc.perform(
                post("/api/v1/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        );

        // then
        resultActions
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.fieldErrors.name").value("2~20자의 한글, 영문 대/소문자만 사용 가능합니다."))
                .andDo(print());
    }

    @DisplayName("[4] 비밀번호는 8~16자의 영문 대/소문자, 숫자, 특수문자를 사용해주세요. (적어도 하나의 영문 소문자, 숫자 포함)")
    @Test
    void passwordValidError() throws Exception {
        // given
        SignUpReq.General request = new SignUpReq.General("pennyway", "페니웨이", "pennyway", "010-1234-5678", "123456");

        // when
        ResultActions resultActions = mockMvc.perform(
                post("/api/v1/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        );

        // then
        resultActions
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.fieldErrors.password").value("8~16자의 영문 대/소문자, 숫자, 특수문자를 사용해주세요. (적어도 하나의 영문 소문자, 숫자 포함)"))
                .andDo(print());
    }

    @DisplayName("[5] 전화번호는 010 혹은 011로 시작하는, 010-0000-0000 형식이어야 합니다.")
    @Test
    void phoneValidError() throws Exception {
        // given
        SignUpReq.General request = new SignUpReq.General("pennyway", "페니웨이", "pennyway1234", "01012345673", "123456");

        // when
        ResultActions resultActions = mockMvc.perform(
                post("/api/v1/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        );

        // then
        resultActions
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.fieldErrors.phone").value("전화번호 형식이 올바르지 않습니다."))
                .andDo(print());
    }

    @DisplayName("[6] 인증번호는 6자리 숫자여야 합니다.")
    @Test
    void codeValidError() throws Exception {
        // given
        SignUpReq.General request = new SignUpReq.General("pennyway", "페니웨이", "pennyway1234", "010-1234-5678", "12345");

        // when
        ResultActions resultActions = mockMvc.perform(
                post("/api/v1/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        );

        // then
        resultActions
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.fieldErrors.code").value("인증번호는 6자리 숫자여야 합니다."))
                .andDo(print());
    }

    @DisplayName("[7] 일부 필드 누락")
    @Test
    void someFieldMissingError() throws Exception {
        // given
        SignUpReq.General request = new SignUpReq.General("pennyway", "페니웨이", "pennyway1234", "010-1234-5678", "123456");

        // when
        ResultActions resultActions = mockMvc.perform(
                post("/api/v1/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)
                                .replace("\"username\":\"pennyway\",", "")
                                .replace("\"phone\":\"010-1234-5678\",", "")));

        // then
        resultActions
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.fieldErrors.username").value("아이디를 입력해주세요"))
                .andExpect(jsonPath("$.fieldErrors.phone").value("전화번호를 입력해주세요"))
                .andDo(print());
    }

    @DisplayName("[8] 정상적인 회원가입 요청 - 쿠키/인증 헤더와 회원 pk 반환")
    @Test
    void signUp() throws Exception {
        // given
        SignUpReq.General request = new SignUpReq.General("pennyway", "페니웨이", "pennyway1234", "010-1234-5678", "123456");
        ResponseCookie expectedCookie = ResponseCookie.from("refreshToken", "refreshToken").maxAge(Duration.ofDays(7).toSeconds()).httpOnly(true).path("/").build();

        given(authUseCase.signUp(request))
                .willReturn(Pair.of(1L, Jwts.of("accessToken", "refreshToken")));
        given(cookieUtil.createCookie("refreshToken", "refreshToken", Duration.ofDays(7).toSeconds()))
                .willReturn(expectedCookie);

        // when
        ResultActions resultActions = mockMvc.perform(
                post("/api/v1/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        );

        // then
        resultActions
                .andExpect(status().isOk())
                .andExpect(header().string("Set-Cookie", expectedCookie.toString()))
                .andExpect(header().string("Authorization", "accessToken"))
                .andExpect(jsonPath("$.data.user.id").value(1))
                .andDo(print());
    }
}
