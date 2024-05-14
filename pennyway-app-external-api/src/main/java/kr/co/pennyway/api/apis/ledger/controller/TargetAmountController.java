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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v2/targets")
public class TargetAmountController implements TargetAmountApi {
    private final TargetAmountUseCase targetAmountUseCase;

    @Override
    @PutMapping("")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> putTargetAmount(@Validated TargetAmountDto.UpdateParamReq param, @AuthenticationPrincipal SecurityUserDetails user) {
        if (!isValidDateForYearAndMonth(param.date())) {
            throw new TargetAmountErrorException(TargetAmountErrorCode.INVALID_TARGET_AMOUNT_DATE);
        }

        targetAmountUseCase.updateTargetAmount(user.getUserId(), param.date(), param.amount());
        return ResponseEntity.ok(SuccessResponse.noContent());
    }

    @GetMapping("/today")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getTargetAmountAndTotalSpending(@Validated TargetAmountDto.GetParamReq param, @AuthenticationPrincipal SecurityUserDetails user) {
        return ResponseEntity.ok(SuccessResponse.from(targetAmountUseCase.getTargetAmountAndTotalSpending(user.getUserId(), param.date())));
    }

    @GetMapping("")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getTargetAmountsAndTotalSpendings(@AuthenticationPrincipal SecurityUserDetails user) {
        return ResponseEntity.ok(SuccessResponse.from("targetAmounts", targetAmountUseCase.getTargetAmountsAndTotalSpendings(user.getUserId())));
    }

    private boolean isValidDateForYearAndMonth(LocalDate date) {
        LocalDate now = LocalDate.now();
        return date.getYear() == now.getYear() && date.getMonth() == now.getMonth();
    }
}
