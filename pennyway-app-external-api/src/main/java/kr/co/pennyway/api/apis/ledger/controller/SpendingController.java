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

    @Override
    @PostMapping("")
    @PreAuthorize("isAuthenticated() and @spendingCategoryManager.hasPermission(#user.getUserId(), #request.categoryId())")
    public ResponseEntity<?> postSpending(@RequestBody @Validated SpendingReq request, @AuthenticationPrincipal SecurityUserDetails user) {
        if (!isValidCategoryIdAndIcon(request.categoryId(), request.icon())) {
            throw new SpendingErrorException(SpendingErrorCode.INVALID_ICON_WITH_CATEGORY_ID);
        }

        return ResponseEntity.ok(SuccessResponse.from("spending", spendingUseCase.createSpending(user.getUserId(), request)));
    }

    @Override
    @GetMapping("")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getSpendingListAtYearAndMonth(@RequestParam("year") int year, @RequestParam("month") int month, @AuthenticationPrincipal SecurityUserDetails user) {
        return ResponseEntity.ok(SuccessResponse.from("spendings", spendingUseCase.getSpendingsAtYearAndMonth(user.getUserId(), year, month)));
    }

    @Override
    @GetMapping("/{spendingId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getSpendingDetail(@PathVariable Long spendingId, @AuthenticationPrincipal SecurityUserDetails user) {
        return ResponseEntity.ok(SuccessResponse.from("spending", spendingUseCase.getSpedingDetail(user.getUserId(), spendingId)));
    }

    /**
     * categoryId가 -1이면 서비스에서 정의한 카테고리를 사용하므로 저장하려는 지출 내역의 icon은 OTHER가 될 수 없고, <br/>
     * categoryId가 -1이 아니면 사용자가 정의한 카테고리를 사용하므로 저장하려는 지출 내역의 icon은 OTHER임을 확인한다.
     *
     * @param categoryId : 사용자가 정의한 카테고리 ID
     * @param icon       : 지출 내역으로 저장하려는 카테고리의 아이콘
     */
    private boolean isValidCategoryIdAndIcon(Long categoryId, SpendingCategory icon) {
        return (categoryId.equals(-1L) && !icon.equals(SpendingCategory.OTHER) || categoryId > 0 && icon.equals(SpendingCategory.OTHER));
    }
}
