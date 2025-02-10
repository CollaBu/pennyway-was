package kr.co.pennyway.api.apis.ledger.service;

import kr.co.pennyway.api.apis.ledger.dto.SpendingReq;
import kr.co.pennyway.api.config.fixture.UserFixture;
import kr.co.pennyway.domain.context.finance.service.SpendingCategoryService;
import kr.co.pennyway.domain.context.finance.service.SpendingService;
import kr.co.pennyway.domain.domains.spending.domain.Spending;
import kr.co.pennyway.domain.domains.spending.domain.SpendingCustomCategory;
import kr.co.pennyway.domain.domains.spending.exception.SpendingErrorException;
import kr.co.pennyway.domain.domains.spending.type.SpendingCategory;
import kr.co.pennyway.domain.domains.user.domain.User;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;

@Slf4j
@ExtendWith(MockitoExtension.class)
public class SpendingUpdateServiceTest {
    private SpendingUpdateService spendingUpdateService;
    @Mock
    private SpendingCategoryService spendingCategoryService;
    @Mock
    private SpendingService spendingService;

    private Spending spending;
    private Spending spendingWithCustomCategory;
    private SpendingReq request;
    private SpendingReq requestWithCustomCategory;
    private User user;
    private SpendingCustomCategory customCategory;


    @BeforeEach
    void setUp() {
        spendingUpdateService = new SpendingUpdateService(spendingService, spendingCategoryService);

        request = new SpendingReq(10000, -1L, SpendingCategory.FOOD, LocalDate.now(), "소비처", "메모");
        requestWithCustomCategory = new SpendingReq(10000, 1L, SpendingCategory.CUSTOM, LocalDate.now(), "소비처", "메모");

        user = UserFixture.GENERAL_USER.toUser();

        customCategory = SpendingCustomCategory.of("커스텀카테고리", SpendingCategory.FOOD, user);

        spending = request.toEntity(user);
        spendingWithCustomCategory = requestWithCustomCategory.toEntity(user, customCategory);
    }

    @DisplayName("없는 사용자 정의 카테고리로 지출 내역을 수정하려고 할 때 SpendingErrorException을 발생시킨다.")
    @Test
    void testUpdateSpendingWithCustomCategoryNotFound() {
        // given
        Long spendingId = 1L;
        given(spendingService.readSpending(spendingId)).willReturn(Optional.of(spending));
        given(spendingCategoryService.readSpendingCustomCategory(1L)).willReturn(Optional.empty());

        // when - then
        SpendingErrorException exception = assertThrows(SpendingErrorException.class, () -> {
            spendingUpdateService.updateSpending(spendingId, requestWithCustomCategory);
        });
        log.debug(exception.getExplainError());
    }

    @DisplayName("커스텀 카테고리를 사용한 지출 내역으로 수정할 시, 커스텀 카테고리를 포함하는 지출내역으로 Spending 객체가 수정 된다.")
    @Test
    void testUpdateSpendingWithCustomCategory() {
        // given
        Long spendingId = 1L;
        given(spendingService.readSpending(spendingId)).willReturn(Optional.of(spending));
        given(spendingCategoryService.readSpendingCustomCategory(1L)).willReturn(Optional.of(customCategory));

        // when - then
        assertDoesNotThrow(() -> spendingUpdateService.updateSpending(spendingId, requestWithCustomCategory));
        assertNotNull(spending.getSpendingCustomCategory());
    }

    @DisplayName("시스템 카테고리를 사용한 지출내역으로 수정할 시, Spending 객체가 수정된다.")
    @Test
    void testUpdateSpendingWithNonCustomCategory() {
        // given
        Long spendingId = 1L;
        given(spendingService.readSpending(spendingId)).willReturn(Optional.of(spending));

        // when - then
        assertDoesNotThrow(() -> spendingUpdateService.updateSpending(spendingId, request));
        assertNull(spending.getSpendingCustomCategory());
    }
}