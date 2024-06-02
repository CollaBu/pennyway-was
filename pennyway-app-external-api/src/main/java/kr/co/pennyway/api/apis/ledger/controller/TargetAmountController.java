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
    private final TargetAmountUseCase targetAmountUseCase;

    @Override
    @PostMapping("")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> postTargetAmount(@RequestParam int year, @RequestParam int month, @AuthenticationPrincipal SecurityUserDetails user) {
        if (YearMonth.of(year, month).isAfter(YearMonth.now())) {
            throw new TargetAmountErrorException(TargetAmountErrorCode.INVALID_TARGET_AMOUNT_DATE);
        }

        return ResponseEntity.ok(SuccessResponse.from("targetAmount", targetAmountUseCase.createTargetAmount(user.getUserId(), year, month)));
    }

    @Override
    @GetMapping("/{date}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getTargetAmountAndTotalSpending(@PathVariable LocalDate date, @AuthenticationPrincipal SecurityUserDetails user) {
        return ResponseEntity.ok(SuccessResponse.from("targetAmount", targetAmountUseCase.getTargetAmountAndTotalSpending(user.getUserId(), date)));
    }

    @Override
    @GetMapping("")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getTargetAmountsAndTotalSpendings(@Validated TargetAmountDto.DateParam param, @AuthenticationPrincipal SecurityUserDetails user) {
        return ResponseEntity.ok(SuccessResponse.from("targetAmounts", targetAmountUseCase.getTargetAmountsAndTotalSpendings(user.getUserId(), param.date())));
    }

    @Override
    @PatchMapping("/{target_amount_id}")
    @PreAuthorize("isAuthenticated() and @targetAmountManager.hasPermission(#user.getUserId(), #targetAmountId)")
    public ResponseEntity<?> patchTargetAmount(TargetAmountDto.AmountParam param, @PathVariable Long targetAmountId, @AuthenticationPrincipal SecurityUserDetails user) {
        targetAmountUseCase.updateTargetAmount(user.getUserId(), targetAmountId, param.amount());
        return ResponseEntity.ok(SuccessResponse.noContent());
    }

    @Override
    @DeleteMapping("/{target_amount_id}")
    @PreAuthorize("isAuthenticated() and @targetAmountManager.hasPermission(#user.getUserId(), #targetAmountId)")
    public ResponseEntity<?> deleteTargetAmount(@PathVariable Long targetAmountId, @AuthenticationPrincipal SecurityUserDetails user) {
        targetAmountUseCase.deleteTargetAmount(user.getUserId(), targetAmountId);
        return ResponseEntity.ok(SuccessResponse.noContent());
    }
}
