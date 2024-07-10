package kr.co.pennyway.api.apis.ledger.controller;

import kr.co.pennyway.api.apis.ledger.api.SpendingApi;
import kr.co.pennyway.api.apis.ledger.dto.SpendingIdsDto;
import kr.co.pennyway.api.apis.ledger.dto.SpendingMigrateDto;
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
    private static final String SPENDING = "spending";

    private final SpendingUseCase spendingUseCase;

    @Override
    @PostMapping("")
    @PreAuthorize("isAuthenticated() and @spendingCategoryManager.hasPermissionExceptMinus(#user.getUserId(), #request.categoryId())")
    public ResponseEntity<?> postSpending(@RequestBody @Validated SpendingReq request, @AuthenticationPrincipal SecurityUserDetails user) {
        if (!isValidCategoryIdAndIcon(request.categoryId(), request.icon())) {
            throw new SpendingErrorException(SpendingErrorCode.INVALID_ICON_WITH_CATEGORY_ID);
        }

        return ResponseEntity.ok(SuccessResponse.from(SPENDING, spendingUseCase.createSpending(user.getUserId(), request)));
    }

    @Override
    @GetMapping("")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getSpendingListAtYearAndMonth(@RequestParam("year") int year, @RequestParam("month") int month, @AuthenticationPrincipal SecurityUserDetails user) {
        return ResponseEntity.ok(SuccessResponse.from(SPENDING, spendingUseCase.getSpendingsAtYearAndMonth(user.getUserId(), year, month)));
    }

    @Override
    @GetMapping("/{spendingId}")
    @PreAuthorize("isAuthenticated() and @spendingManager.hasPermission(#user.getUserId(), #spendingId)")
    public ResponseEntity<?> getSpendingDetail(@PathVariable Long spendingId, @AuthenticationPrincipal SecurityUserDetails user) {
        return ResponseEntity.ok(SuccessResponse.from(SPENDING, spendingUseCase.getSpedingDetail(spendingId)));
    }

    @Override
    @PutMapping("/{spendingId}")
    @PreAuthorize("isAuthenticated() and @spendingManager.hasPermission(#user.getUserId(), #spendingId)")
    public ResponseEntity<?> updateSpending(@PathVariable Long spendingId, @RequestBody @Validated SpendingReq request, @AuthenticationPrincipal SecurityUserDetails user) {
        if (!isValidCategoryIdAndIcon(request.categoryId(), request.icon())) {
            throw new SpendingErrorException(SpendingErrorCode.INVALID_ICON_WITH_CATEGORY_ID);
        }

        return ResponseEntity.ok(SuccessResponse.from(SPENDING, spendingUseCase.updateSpending(spendingId, request)));
    }

    @Override
    @DeleteMapping("/{spendingId}")
    @PreAuthorize("isAuthenticated() and @spendingManager.hasPermission(#user.getUserId(), #spendingId)")
    public ResponseEntity<?> deleteSpending(@PathVariable Long spendingId, @AuthenticationPrincipal SecurityUserDetails user) {
        spendingUseCase.deleteSpending(spendingId);

        return ResponseEntity.ok(SuccessResponse.noContent());
    }

    @Override
    @DeleteMapping("")
    @PreAuthorize("isAuthenticated() and @spendingManager.hasPermissions(#user.getUserId(), #spendingIds.spendingIds())")
    public ResponseEntity<?> deleteSpendings(@RequestBody SpendingIdsDto spendingIds, @AuthenticationPrincipal SecurityUserDetails user) {
        spendingUseCase.deleteSpendings(spendingIds.spendingIds());
        return ResponseEntity.ok(SuccessResponse.noContent());
    }

    @Override
    @PatchMapping({"migration/{fromCategoryId}"})
    @PreAuthorize("isAuthenticated()") // TODO: 권한검사 추가 필요 (ToCategory가 커스텀 예외인가? 에 따라 권한검사가 필요하거나 필요없으므로 SpEL로 한번만에 안됨
    public ResponseEntity<?> migrateSpendings(@PathVariable Long fromCategoryId,
                                              @RequestBody SpendingMigrateDto request) {
        spendingUseCase.migrateSpendings(fromCategoryId, request);

        return ResponseEntity.ok(SuccessResponse.noContent());
    }

    /**
     * categoryId가 -1이면 서비스에서 정의한 카테고리를 사용하므로 저장하려는 지출 내역의 icon은 CUSTOM이나 OTHER이 될 수 없고, <br/>
     * categoryId가 -1이 아니면 사용자가 정의한 카테고리를 사용하므로 저장하려는 지출 내역의 icon은 CUSTOM임을 확인한다.
     *
     * @param categoryId : 사용자가 정의한 카테고리 ID
     * @param icon       : 지출 내역으로 저장하려는 카테고리의 아이콘
     */
    private boolean isValidCategoryIdAndIcon(Long categoryId, SpendingCategory icon) {
        return (categoryId.equals(-1L) && (!icon.equals(SpendingCategory.CUSTOM) && !icon.equals(SpendingCategory.OTHER))) || (categoryId > 0 && icon.equals(SpendingCategory.CUSTOM));
    }
}
