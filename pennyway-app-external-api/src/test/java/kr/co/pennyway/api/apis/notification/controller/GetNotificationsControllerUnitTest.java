package kr.co.pennyway.api.apis.notification.controller;

import kr.co.pennyway.api.apis.notification.usecase.NotificationUseCase;
import kr.co.pennyway.api.config.WebConfig;
import kr.co.pennyway.api.config.supporter.WithSecurityMockUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {NotificationController.class}, excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = WebConfig.class)})
@ActiveProfiles("test")
public class GetNotificationsControllerUnitTest {
    @Autowired
    private MockMvc mockMvc;
    @Mock
    private NotificationUseCase notificationUseCase;

    @BeforeEach
    void setUp(WebApplicationContext context) {
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .defaultRequest(post("/**").with(csrf()))
                .defaultRequest(put("/**").with(csrf()))
                .defaultRequest(delete("/**").with(csrf()))
                .build();
    }

    @Test
    @WithSecurityMockUser
    @DisplayName("쿼리 파라미터로 page 외의 파라미터는 기본값을 갖는다.")
    void getNotificationsWithDefaultParameters() throws Exception {
        // when
        given(notificationUseCase.getNotifications(1L, any())).willReturn(List.of(NotificationRes.from(NotificationFixture.DAILY_SPENDING.toEntity())));

        // when
        ResultActions result = performGetNotifications(1);

        // then
        result.andExpect(status().isOk());
    }

    private ResultActions performGetNotifications(int page) throws Exception {
        return mockMvc.perform(get("/v2/notifications")
                .param("page", String.valueOf(page)));
    }
}
