package kr.co.pennyway.api.apis.ledger.controller;

import kr.co.pennyway.domain.common.redis.sign.SignEventLogService;
import kr.co.pennyway.infra.common.jwt.JwtProvider;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

@WebMvcTest(SpendingCategoryController.class)
@ActiveProfiles("test")
public class SpendingCategoryUpdateControllerTest {
    @MockBean
    private SignEventLogService signEventLogService;
    @MockBean
    private JwtProvider accessTokenProvider;
}
