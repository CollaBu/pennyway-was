package kr.co.pennyway.api.apis.users.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.co.pennyway.api.apis.users.dto.DeviceTokenDto;
import kr.co.pennyway.api.apis.users.dto.UserProfileUpdateDto;
import kr.co.pennyway.api.apis.users.usecase.UserAccountUseCase;
import kr.co.pennyway.api.config.WebConfig;
import kr.co.pennyway.api.config.fixture.DeviceFixture;
import kr.co.pennyway.api.config.supporter.WithSecurityMockUser;
import kr.co.pennyway.common.exception.StatusCode;
import kr.co.pennyway.domain.domains.user.exception.UserErrorCode;
import kr.co.pennyway.domain.domains.user.exception.UserErrorException;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static kr.co.pennyway.common.exception.ReasonCode.REQUIRED_PARAMETERS_MISSING_IN_REQUEST_BODY;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {UserAccountController.class}, excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = WebConfig.class)})
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
                .defaultRequest(patch("/**").with(csrf()))
                .build();
    }

    @Nested
    @Order(1)
    @DisplayName("[1] 디바이스 요청 테스트")
    class DeviceTokenRequestTest {
        @DisplayName("디바이스가 정상적으로 저장되었을 때, 디바이스 pk와 등록된 토큰을 반환한다.")
        @Test
        @WithSecurityMockUser
        void putDevice() throws Exception {
            // given
            DeviceTokenDto.RegisterReq request = DeviceFixture.INIT.toRegisterReq();
            DeviceTokenDto.RegisterRes expectedResponse = new DeviceTokenDto.RegisterRes(2L, "originToken");
            given(userAccountUseCase.registerDeviceToken(1L, request)).willReturn(expectedResponse);

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
    @DisplayName("[2] 사용자 이름 수정 테스트")
    class UpdateNameTest {
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
            return mockMvc.perform(patch("/v2/users/me/name")
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(request)));
        }
    }

    @Nested
    @Order(3)
    @DisplayName("[3] 사용자 닉네임 수정 테스트")
    class UpdateNicknameTest {
        @DisplayName("사용자 닉네임 수정 요청 시, 유효성 검사에 실패하면 422 에러를 반환한다.")
        @Test
        @WithSecurityMockUser
        void updateNicknameValidationFail() throws Exception {
            // given
            String newNicknameWithBlank = " ";
            String newNicknameWithOverLength = "한글이름";
            String newNicknameWithSpecialCharacter = "hello!";
            String newNicknameWithWhiteSpace = "jay ang";
            String newNicknameWithOverLengthAndWhiteSpace = "myNameisJayangHello";
            String expectedErrorCode = String.valueOf(StatusCode.UNPROCESSABLE_CONTENT.getCode() * 10 + REQUIRED_PARAMETERS_MISSING_IN_REQUEST_BODY.getCode());

            // when
            ResultActions result1 = performUpdateNicknameRequest(newNicknameWithBlank);
            ResultActions result2 = performUpdateNicknameRequest(newNicknameWithOverLength);
            ResultActions result3 = performUpdateNicknameRequest(newNicknameWithSpecialCharacter);
            ResultActions result4 = performUpdateNicknameRequest(newNicknameWithWhiteSpace);
            ResultActions result5 = performUpdateNicknameRequest(newNicknameWithOverLengthAndWhiteSpace);

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
            result4.andExpect(status().isUnprocessableEntity())
                    .andExpect(jsonPath("$.code").value(expectedErrorCode))
                    .andDo(print());
            result5.andExpect(status().isUnprocessableEntity())
                    .andExpect(jsonPath("$.code").value(expectedErrorCode))
                    .andDo(print());
        }

        @DisplayName("사용자 닉네임 수정 요청 시, 삭제된 사용자인 경우 404 에러를 반환한다.")
        @Test
        @WithSecurityMockUser
        void updateNicknameDeletedUser() throws Exception {
            // given
            String newNickname = "jayang._.";
            willThrow(new UserErrorException(UserErrorCode.NOT_FOUND)).given(userAccountUseCase).updateUsername(1L, newNickname);

            // when
            ResultActions result = performUpdateNicknameRequest(newNickname);

            // then
            result.andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value(UserErrorCode.NOT_FOUND.causedBy().getCode()))
                    .andExpect(jsonPath("$.message").value(UserErrorCode.NOT_FOUND.getExplainError()))
                    .andDo(print());
        }

        @DisplayName("사용자 닉네임 수정 요청 시, 사용자 닉네임이 정상적으로 수정되면 200 코드를 반환한다.")
        @Test
        @WithSecurityMockUser
        void updateNicknameSuccess() throws Exception {
            // given
            String newNickname = "jayang._.";

            // when
            ResultActions result = performUpdateNicknameRequest(newNickname);

            // then
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("2000"))
                    .andDo(print());
        }

        private ResultActions performUpdateNicknameRequest(String newNickname) throws Exception {
            UserProfileUpdateDto.UsernameReq request = new UserProfileUpdateDto.UsernameReq(newNickname);
            return mockMvc.perform(patch("/v2/users/me/username")
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(request)));
        }
    }

    @Nested
    @Order(4)
    @DisplayName("[4] 사용자 비밀번호 검증 테스트")
    class VerifyNicknameTest {
        @DisplayName("사용자 현재 비밀번호 검증 시, 빈 문자열이면 422 에러를 반환한다.")
        @Test
        @WithSecurityMockUser
        void verifyPasswordValidationFail() throws Exception {
            // given
            String currentPasswordWithBlank = " ";
            String expectedErrorCode = String.valueOf(StatusCode.UNPROCESSABLE_CONTENT.getCode() * 10 + REQUIRED_PARAMETERS_MISSING_IN_REQUEST_BODY.getCode());

            // when
            ResultActions result = performVerifyCurrentPasswordRequest(currentPasswordWithBlank);

            // then
            result.andExpect(status().isUnprocessableEntity())
                    .andExpect(jsonPath("$.code").value(expectedErrorCode))
                    .andDo(print());
        }

        @DisplayName("사용자 현재 비밀번호 검증 시, 삭제된 사용자인 경우 404 에러를 반환한다.")
        @Test
        @WithSecurityMockUser
        void verifyCurrentPasswordDeletedUser() throws Exception {
            // given
            String currentPassword = "currentPassword";
            willThrow(new UserErrorException(UserErrorCode.NOT_FOUND)).given(userAccountUseCase).verifyPassword(1L, currentPassword);

            // when
            ResultActions result = performVerifyCurrentPasswordRequest(currentPassword);

            // then
            result.andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value(UserErrorCode.NOT_FOUND.causedBy().getCode()))
                    .andExpect(jsonPath("$.message").value(UserErrorCode.NOT_FOUND.getExplainError()))
                    .andDo(print());
        }

        @DisplayName("사용자 현재 비밀번호 검증 시, 일반 회원가입 이력이 없는 경우 403 에러를 반환한다.")
        @Test
        @WithSecurityMockUser
        void verifyCurrentPasswordSocialUser() throws Exception {
            // given
            String currentPassword = "currentPassword";
            willThrow(new UserErrorException(UserErrorCode.DO_NOT_GENERAL_SIGNED_UP)).given(userAccountUseCase).verifyPassword(1L, currentPassword);

            // when
            ResultActions result = performVerifyCurrentPasswordRequest(currentPassword);

            // then
            result.andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value(UserErrorCode.DO_NOT_GENERAL_SIGNED_UP.causedBy().getCode()))
                    .andExpect(jsonPath("$.message").value(UserErrorCode.DO_NOT_GENERAL_SIGNED_UP.getExplainError()))
                    .andDo(print());
        }

        @DisplayName("사용자 현재 비밀번호 검증 시, 비밀번호가 일치하지 않으면 400 에러를 반환한다.")
        @Test
        @WithSecurityMockUser
        void verifyCurrentPasswordFail() throws Exception {
            // given
            String currentPassword = "currentPassword";
            willThrow(new UserErrorException(UserErrorCode.NOT_MATCHED_PASSWORD)).given(userAccountUseCase).verifyPassword(1L, currentPassword);

            // when
            ResultActions result = performVerifyCurrentPasswordRequest(currentPassword);

            // then
            result.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(UserErrorCode.NOT_MATCHED_PASSWORD.causedBy().getCode()))
                    .andExpect(jsonPath("$.message").value(UserErrorCode.NOT_MATCHED_PASSWORD.getExplainError()))
                    .andDo(print());
        }

        @DisplayName("사용자 현재 비밀번호 검증 시, 비밀번호가 일치하면 200 코드를 반환한다.")
        @Test
        @WithSecurityMockUser
        void verifyCurrentPasswordSuccess() throws Exception {
            // given
            String currentPassword = "currentPassword";

            // when
            ResultActions result = performVerifyCurrentPasswordRequest(currentPassword);

            // then
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("2000"))
                    .andDo(print());
        }

        private ResultActions performVerifyCurrentPasswordRequest(String currentPassword) throws Exception {
            UserProfileUpdateDto.PasswordVerificationReq request = new UserProfileUpdateDto.PasswordVerificationReq(currentPassword);
            return mockMvc.perform(post("/v2/users/me/password/verification")
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(request)));
        }

    }

    @Nested
    @Order(5)
    @DisplayName("[5] 사용자 비밀번호 수정 테스트")
    class UpdatePasswordTest {
        String oldPassword = "oldPassword1";
        String newPassword = "newPassword1";

        @DisplayName("비밀번호가 8~16자의 영문 대/소문, 숫자, 특수문자(이모티콘, 공백 사용 불가, 적어도 하나 이상의 소문자 알파벳과 숫자 포함)가 아니면 422 에러를 반환한다.")
        @Test
        @WithSecurityMockUser
        void updatePasswordValidationFail() throws Exception {
            // given
            String oldPassword = "oldPassword";
            String newPasswordWithBlank = " ";
            String newPasswordWithUnderLength = "short";
            String newPasswordWithOverLength = "passwordpasswordpasswordpassword";
            String newPasswordWithOnlyAlphabet = "passwordpassword";
            String newPasswordWithOnlyNumber = "1234567890";
            String newPasswordWithOnlySpecialCharacter = "!@#$%^&*()";
            String newPasswordWithOnlyUpperCase = "PASSWORDPASSWORD";
            String newPasswordWithOnlyLowerCase = "passwordpassword";
            String newPasswordWithOnlyEmoji = "😊😊😊😊😊😊😊😊";
            String newPasswordWithOnlyWhiteSpace = "password password";
            String newPasswordWithOnlySpecialCharacterAndWhiteSpace = "!@#$%^&*() ";
            String newPasswordWithOnlySpecialCharacterAndEmoji = "!@#$%^&*()😊";
            String newPasswordWithOnlySpecialCharacterAndEmojiAndWhiteSpace = "!@#$%^&*() 😊";
            List<String> newPasswords = List.of(newPasswordWithBlank, newPasswordWithUnderLength, newPasswordWithOverLength, newPasswordWithOnlyAlphabet, newPasswordWithOnlyNumber, newPasswordWithOnlySpecialCharacter, newPasswordWithOnlyUpperCase, newPasswordWithOnlyLowerCase, newPasswordWithOnlyEmoji, newPasswordWithOnlyWhiteSpace, newPasswordWithOnlySpecialCharacterAndWhiteSpace, newPasswordWithOnlySpecialCharacterAndEmoji, newPasswordWithOnlySpecialCharacterAndEmojiAndWhiteSpace);

            String expectedErrorCode = String.valueOf(StatusCode.UNPROCESSABLE_CONTENT.getCode() * 10 + REQUIRED_PARAMETERS_MISSING_IN_REQUEST_BODY.getCode());

            // when - then
            for (String newPassword : newPasswords) {
                ResultActions result = performUpdatePasswordRequest(oldPassword, newPassword);
                result.andExpect(status().isUnprocessableEntity())
                        .andExpect(jsonPath("$.code").value(expectedErrorCode))
                        .andDo(print());
            }
        }

        @DisplayName("비밀번호가 현재 비밀번호와 동일하면 400 에러를 반환한다.")
        @Test
        @WithSecurityMockUser
        void updatePasswordSamePassword() throws Exception {
            // given
            willThrow(new UserErrorException(UserErrorCode.PASSWORD_NOT_CHANGED)).given(userAccountUseCase).updatePassword(1L, oldPassword, newPassword);

            // when
            ResultActions result = performUpdatePasswordRequest(oldPassword, newPassword);

            // then
            result.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(UserErrorCode.PASSWORD_NOT_CHANGED.causedBy().getCode()))
                    .andExpect(jsonPath("$.message").value(UserErrorCode.PASSWORD_NOT_CHANGED.getExplainError()))
                    .andDo(print());
        }

        @DisplayName("기존 비밀번호가 일치하지 않으면 400 에러를 반환한다.")
        @Test
        @WithSecurityMockUser
        void updatePasswordFail() throws Exception {
            // given
            willThrow(new UserErrorException(UserErrorCode.NOT_MATCHED_PASSWORD)).given(userAccountUseCase).updatePassword(1L, oldPassword, newPassword);

            // when
            ResultActions result = performUpdatePasswordRequest(oldPassword, newPassword);

            // then
            result.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(UserErrorCode.NOT_MATCHED_PASSWORD.causedBy().getCode()))
                    .andExpect(jsonPath("$.message").value(UserErrorCode.NOT_MATCHED_PASSWORD.getExplainError()))
                    .andDo(print());
        }

        @DisplayName("사용자가 삭제된 사용자인 경우 404 에러를 반환한다.")
        @Test
        @WithSecurityMockUser
        void updatePasswordDeletedUser() throws Exception {
            // given
            willThrow(new UserErrorException(UserErrorCode.NOT_FOUND)).given(userAccountUseCase).updatePassword(1L, oldPassword, newPassword);

            // when
            ResultActions result = performUpdatePasswordRequest(oldPassword, newPassword);

            // then
            result.andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value(UserErrorCode.NOT_FOUND.causedBy().getCode()))
                    .andExpect(jsonPath("$.message").value(UserErrorCode.NOT_FOUND.getExplainError()))
                    .andDo(print());
        }

        @DisplayName("사용자가 일반 회원가입 이력이 없는 경우 403 에러를 반환한다.")
        @Test
        @WithSecurityMockUser
        void updatePasswordSocialUser() throws Exception {
            // given
            willThrow(new UserErrorException(UserErrorCode.DO_NOT_GENERAL_SIGNED_UP)).given(userAccountUseCase).updatePassword(1L, oldPassword, newPassword);

            // when
            ResultActions result = performUpdatePasswordRequest(oldPassword, newPassword);

            // then
            result.andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value(UserErrorCode.DO_NOT_GENERAL_SIGNED_UP.causedBy().getCode()))
                    .andExpect(jsonPath("$.message").value(UserErrorCode.DO_NOT_GENERAL_SIGNED_UP.getExplainError()))
                    .andDo(print());
        }

        @DisplayName("비밀번호가 정상적으로 수정되면 200 코드를 반환한다.")
        @Test
        @WithSecurityMockUser
        void updatePasswordSuccess() throws Exception {
            // when
            ResultActions result = performUpdatePasswordRequest(oldPassword, newPassword);

            // then
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("2000"))
                    .andDo(print());
        }

        private ResultActions performUpdatePasswordRequest(String oldPassword, String newPassword) throws Exception {
            UserProfileUpdateDto.PasswordReq request = new UserProfileUpdateDto.PasswordReq(oldPassword, newPassword);
            return mockMvc.perform(patch("/v2/users/me/password")
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(request)));
        }
    }

    @Nested
    @Order(6)
    @DisplayName("[6] 사용자 계정 삭제 테스트")
    class DeleteAccountTest {
        @DisplayName("사용자 계정 삭제 요청 시, 삭제된 사용자인 경우 404 에러를 반환한다.")
        @Test
        @WithSecurityMockUser
        void deleteAccountDeletedUser() throws Exception {
            // given
            willThrow(new UserErrorException(UserErrorCode.NOT_FOUND)).given(userAccountUseCase).deleteAccount(1L);

            // when
            ResultActions result = performDeleteAccountRequest();

            // then
            result.andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value(UserErrorCode.NOT_FOUND.causedBy().getCode()))
                    .andExpect(jsonPath("$.message").value(UserErrorCode.NOT_FOUND.getExplainError()))
                    .andDo(print());
        }

        @DisplayName("사용자 계정 삭제 요청 시, 사용자 계정이 정상적으로 삭제되면 200 코드를 반환한다.")
        @Test
        @WithSecurityMockUser
        void deleteAccountSuccess() throws Exception {
            // when
            ResultActions result = performDeleteAccountRequest();

            // then
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("2000"))
                    .andDo(print());
        }

        private ResultActions performDeleteAccountRequest() throws Exception {
            return mockMvc.perform(delete("/v2/users/me")
                    .contentType("application/json"));
        }
    }
}
