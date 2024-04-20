package kr.co.pennyway.api.apis.users.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.co.pennyway.api.apis.users.dto.DeviceDto;
import kr.co.pennyway.api.apis.users.usecase.UserAccountUseCase;
import kr.co.pennyway.api.config.supporter.WithSecurityMockUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {UserAccountController.class})
@ActiveProfiles("local")
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

    @DisplayName("[1] 디바이스가 정상적으로 저장되었을 때, 디바이스 pk와 등록된 토큰을 반환한다.")
    @Test
    @WithSecurityMockUser
    void putDeviceSuccess() throws Exception {
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