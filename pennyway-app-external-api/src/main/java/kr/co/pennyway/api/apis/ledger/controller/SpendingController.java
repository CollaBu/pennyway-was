package kr.co.pennyway.api.apis.ledger.controller;

import kr.co.pennyway.api.apis.ledger.api.SpendingApi;
import kr.co.pennyway.api.apis.ledger.dto.SpendingReq;
import kr.co.pennyway.api.apis.ledger.usecase.SpendingUseCase;
import kr.co.pennyway.api.common.response.SuccessResponse;
import kr.co.pennyway.api.common.security.authentication.SecurityUserDetails;
import kr.co.pennyway.domain.domains.spending.exception.SpendingErrorCode;
import kr.co.pennyway.domain.domains.spending.exception.SpendingErrorException;
import kr.co.pennyway.domain.domains.spending.type.SpendingCategory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v2/spendings")
public class SpendingController implements SpendingApi {
    private final SpendingUseCase spendingUseCase;

    @PostMapping("")
    @PreAuthorize("isAuthenticated()") // categoryId가 -1이 아니면 사용자가 정의한 것인지 확인 필요함
    public ResponseEntity<?> postSpending(@RequestBody @Validated SpendingReq request, @AuthenticationPrincipal SecurityUserDetails user) {
        if (request.icon().equals(SpendingCategory.OTHER) && request.categoryId().equals(-1L)) {
            throw new SpendingErrorException(SpendingErrorCode.INVALID_ICON);
        }

        return ResponseEntity.ok(SuccessResponse.from("spending", spendingUseCase.createSpending(user.getUserId(), request)));
    }

    @Override
    @GetMapping("")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getSpendingListAtYearAndMonth(@RequestParam("year") int year, @RequestParam("month") int month, @AuthenticationPrincipal SecurityUserDetails user) {
        return ResponseEntity.ok(SuccessResponse.from("spendings", spendingUseCase.getSpendingsAtYearAndMonth(user.getUserId(), year, month)));
    }
}
