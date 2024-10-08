package kr.co.pennyway.api.apis.ledger.controller;

import kr.co.pennyway.api.apis.ledger.api.TargetAmountApi;
import kr.co.pennyway.api.apis.ledger.dto.TargetAmountDto;
import kr.co.pennyway.api.apis.ledger.usecase.TargetAmountUseCase;
import kr.co.pennyway.api.common.response.SuccessResponse;
import kr.co.pennyway.api.common.security.authentication.SecurityUserDetails;
import kr.co.pennyway.domain.domains.target.exception.TargetAmountErrorCode;
import kr.co.pennyway.domain.domains.target.exception.TargetAmountErrorException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.YearMonth;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v2/target-amounts")
public class TargetAmountController implements TargetAmountApi {
    private static final String TARGET_AMOUNT = "targetAmount";
    private static final String TARGET_AMOUNTS = "targetAmounts";
    private final TargetAmountUseCase targetAmountUseCase;

    @Override
    @PostMapping("")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> postTargetAmount(@RequestParam int year, @RequestParam int month, @AuthenticationPrincipal SecurityUserDetails user) {
        if (!(year == YearMonth.now().getYear() && month == YearMonth.now().getMonthValue())) {
            throw new TargetAmountErrorException(TargetAmountErrorCode.INVALID_TARGET_AMOUNT_DATE);
        }

        return ResponseEntity.ok(SuccessResponse.from(TARGET_AMOUNT, targetAmountUseCase.createTargetAmount(user.getUserId(), year, month)));
    }

    @Override
    @GetMapping("/{date}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getTargetAmountAndTotalSpending(@PathVariable LocalDate date, @AuthenticationPrincipal SecurityUserDetails user) {
        return ResponseEntity.ok(SuccessResponse.from(TARGET_AMOUNT, targetAmountUseCase.getTargetAmountAndTotalSpending(user.getUserId(), date)));
    }

    @Override
    @GetMapping("")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getTargetAmountsAndTotalSpendings(@Validated TargetAmountDto.DateParam param, @AuthenticationPrincipal SecurityUserDetails user) {
        return ResponseEntity.ok(SuccessResponse.from(TARGET_AMOUNTS, targetAmountUseCase.getTargetAmountsAndTotalSpendings(user.getUserId(), param.date())));
    }

    @Override
    @GetMapping("/recent")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getRecentTargetAmount(@AuthenticationPrincipal SecurityUserDetails user) {
        return ResponseEntity.ok(SuccessResponse.from(TARGET_AMOUNT, targetAmountUseCase.getRecentTargetAmount(user.getUserId())));
    }

    @Override
    @PatchMapping("/{target_amount_id}")
    @PreAuthorize("isAuthenticated() and @targetAmountManager.hasPermission(principal.userId, #targetAmountId)")
    public ResponseEntity<?> patchTargetAmount(@Validated TargetAmountDto.AmountParam param, @PathVariable("target_amount_id") Long targetAmountId) {
        return ResponseEntity.ok(SuccessResponse.from(TARGET_AMOUNT, targetAmountUseCase.updateTargetAmount(targetAmountId, param.amount())));
    }

    @Override
    @DeleteMapping("/{target_amount_id}")
    @PreAuthorize("isAuthenticated() and @targetAmountManager.hasPermission(principal.userId, #targetAmountId)")
    public ResponseEntity<?> deleteTargetAmount(@PathVariable("target_amount_id") Long targetAmountId) {
        targetAmountUseCase.deleteTargetAmount(targetAmountId);
        return ResponseEntity.ok(SuccessResponse.noContent());
    }
}
