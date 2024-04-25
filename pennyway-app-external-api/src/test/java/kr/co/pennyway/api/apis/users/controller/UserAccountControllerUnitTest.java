package kr.co.pennyway.api.apis.users.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.co.pennyway.api.apis.users.dto.DeviceDto;
import kr.co.pennyway.api.apis.users.dto.UserProfileUpdateDto;
import kr.co.pennyway.api.apis.users.usecase.UserAccountUseCase;
import kr.co.pennyway.api.config.supporter.WithSecurityMockUser;
import kr.co.pennyway.common.exception.StatusCode;
import kr.co.pennyway.domain.domains.user.exception.UserErrorCode;
import kr.co.pennyway.domain.domains.user.exception.UserErrorException;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static kr.co.pennyway.common.exception.ReasonCode.REQUIRED_PARAMETERS_MISSING_IN_REQUEST_BODY;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {UserAccountController.class})
@ActiveProfiles("local")
@TestClassOrder(ClassOrderer.OrderAnnotation.class)
public class UserAccountControllerUnitTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserAccountUseCase userAccountUseCase;

    @BeforeEach
    void setUp(WebApplicationContext webApplicationContext) {
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .defaultRequest(put("/**").with(csrf()))
                .defaultRequest(delete("/**").with(csrf()))
                .build();
    }

    @Nested
    @Order(1)
    @DisplayName("[1] 디바이스 요청 테스트")
    class DeviceRequestTest {
        @DisplayName("디바이스가 정상적으로 저장되었을 때, 디바이스 pk와 등록된 토큰을 반환한다.")
        @Test
        @WithSecurityMockUser
        void putDevice() throws Exception {
            // given
            DeviceDto.RegisterReq request = new DeviceDto.RegisterReq("newToken", "newToken", "modelA", "Windows");
            DeviceDto.RegisterRes expectedResponse = new DeviceDto.RegisterRes(2L, "newToken");
            given(userAccountUseCase.registerDevice(1L, request)).willReturn(expectedResponse);

            // when
            ResultActions result = mockMvc.perform(put("/v2/users/me/devices")
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(request)));

            // then
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("2000"))
                    .andExpect(jsonPath("$.data.device.id").value(expectedResponse.id()))
                    .andExpect(jsonPath("$.data.device.token").value(expectedResponse.token()))
                    .andDo(print());
        }
    }

    @Nested
    @Order(2)
    @DisplayName("[2] 사용자 이름, 닉네임 수정 테스트")
    class UpdateUserProfileTest {
        @DisplayName("사용자 이름 수정 요청 시, 유효성 검사에 실패하면 422 에러를 반환한다.")
        @Test
        @WithSecurityMockUser
        void updateNameValidationFail() throws Exception {
            // given
            String newNameWithBlank = " ";
            String newNameWithOverLength = "안녕하세요장페르센입니다";
            String newNameWithSpecialCharacter = "hello!";
            String expectedErrorCode = String.valueOf(StatusCode.UNPROCESSABLE_CONTENT.getCode() * 10 + REQUIRED_PARAMETERS_MISSING_IN_REQUEST_BODY.getCode());

            // when
            ResultActions result1 = performUpdateNameRequest(newNameWithBlank);
            ResultActions result2 = performUpdateNameRequest(newNameWithOverLength);
            ResultActions result3 = performUpdateNameRequest(newNameWithSpecialCharacter);

            // then
            result1.andExpect(status().isUnprocessableEntity())
                    .andExpect(jsonPath("$.code").value(expectedErrorCode))
                    .andDo(print());
            result2.andExpect(status().isUnprocessableEntity())
                    .andExpect(jsonPath("$.code").value(expectedErrorCode))
                    .andDo(print());
            result3.andExpect(status().isUnprocessableEntity())
                    .andExpect(jsonPath("$.code").value(expectedErrorCode))
                    .andDo(print());
        }

        @DisplayName("사용자 이름 수정 요청 시, 삭제된 사용자인 경우 404 에러를 반환한다.")
        @Test
        @WithSecurityMockUser
        void updateNameDeletedUser() throws Exception {
            // given
            String newName = "양재서";
            willThrow(new UserErrorException(UserErrorCode.NOT_FOUND)).given(userAccountUseCase).updateName(1L, newName);

            // when
            ResultActions result = performUpdateNameRequest(newName);

            // then
            result.andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value(UserErrorCode.NOT_FOUND.causedBy().getCode()))
                    .andExpect(jsonPath("$.message").value(UserErrorCode.NOT_FOUND.getExplainError()))
                    .andDo(print());
        }

        @DisplayName("사용자 이름 수정 요청 시, 사용자 이름이 정상적으로 수정되면 200 코드를 반환한다.")
        @Test
        @WithSecurityMockUser
        void updateNameSuccess() throws Exception {
            // given
            String newName = "양재서";

            // when
            ResultActions result = performUpdateNameRequest(newName);

            // then
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("2000"))
                    .andDo(print());
        }

        private ResultActions performUpdateNameRequest(String newName) throws Exception {
            UserProfileUpdateDto.NameReq request = new UserProfileUpdateDto.NameReq(newName);
            return mockMvc.perform(put("/v2/users/me/name")
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(request)));
        }
    }
}
