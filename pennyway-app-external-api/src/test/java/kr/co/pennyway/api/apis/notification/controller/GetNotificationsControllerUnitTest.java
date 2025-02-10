package kr.co.pennyway.api.apis.notification.controller;

import kr.co.pennyway.api.apis.notification.dto.NotificationDto;
import kr.co.pennyway.api.apis.notification.usecase.NotificationUseCase;
import kr.co.pennyway.api.config.fixture.NotificationFixture;
import kr.co.pennyway.api.config.fixture.UserFixture;
import kr.co.pennyway.api.config.supporter.WithSecurityMockUser;
import kr.co.pennyway.domain.domains.notification.domain.Notification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {NotificationController.class})
@ActiveProfiles("test")
public class GetNotificationsControllerUnitTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
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
        int page = 0, currentPageNumber = 0, pageSize = 20, numberOfElements = 1;
        Pageable pa = Pageable.ofSize(pageSize).withPage(currentPageNumber);

        Notification notification = NotificationFixture.ANNOUNCEMENT_DAILY_SPENDING.toEntity(UserFixture.GENERAL_USER.toUser());
        NotificationDto.Info info = NotificationDto.Info.from(notification);

        given(notificationUseCase.getReadNotifications(eq(1L), any())).willReturn(NotificationDto.SliceRes.from(List.of(info), pa, numberOfElements, false));

        // when
        ResultActions result = performGetNotifications(page);

        // then
        result.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.notifications.currentPageNumber").value(page));
    }

    @Test
    @WithSecurityMockUser
    @DisplayName("응답은 무한 스크롤 방식으로 제공되며, content, currentPageNumber, pageSize, numberOfElements, hasNext 필드를 포함한다.")
    void getNotificationsWithInfiniteScroll() throws Exception {
        // when
        int page = 0, currentPageNumber = 0, pageSize = 20, numberOfElements = 1;
        Pageable pa = Pageable.ofSize(pageSize).withPage(currentPageNumber);

        Notification notification = NotificationFixture.ANNOUNCEMENT_DAILY_SPENDING.toEntity(UserFixture.GENERAL_USER.toUser());
        NotificationDto.Info info = NotificationDto.Info.from(notification);
        NotificationDto.SliceRes sliceRes = NotificationDto.SliceRes.from(List.of(info), pa, numberOfElements, false);

        given(notificationUseCase.getReadNotifications(eq(1L), any())).willReturn(sliceRes);

        // when
        ResultActions result = performGetNotifications(page);

        // then
        result.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.notifications.content").exists())
                .andExpect(jsonPath("$.data.notifications.currentPageNumber").value(sliceRes.currentPageNumber()))
                .andExpect(jsonPath("$.data.notifications.pageSize").value(sliceRes.pageSize()))
                .andExpect(jsonPath("$.data.notifications.numberOfElements").value(sliceRes.numberOfElements()))
                .andExpect(jsonPath("$.data.notifications.hasNext").value(sliceRes.hasNext()));
    }

    private ResultActions performGetNotifications(int page) throws Exception {
        return mockMvc.perform(get("/v2/notifications")
                .param("page", String.valueOf(page)));
    }
}
