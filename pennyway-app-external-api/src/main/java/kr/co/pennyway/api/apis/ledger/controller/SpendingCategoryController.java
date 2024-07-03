package kr.co.pennyway.api.apis.ledger.controller;

import kr.co.pennyway.api.apis.ledger.api.SpendingCategoryApi;
import kr.co.pennyway.api.apis.ledger.dto.SpendingCategoryDto;
import kr.co.pennyway.api.apis.ledger.usecase.SpendingCategoryUseCase;
import kr.co.pennyway.api.common.query.SpendingCategoryType;
import kr.co.pennyway.api.common.response.SuccessResponse;
import kr.co.pennyway.api.common.security.authentication.SecurityUserDetails;
import kr.co.pennyway.domain.domains.spending.exception.SpendingErrorCode;
import kr.co.pennyway.domain.domains.spending.exception.SpendingErrorException;
import kr.co.pennyway.domain.domains.spending.type.SpendingCategory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v2/spending-categories")
public class SpendingCategoryController implements SpendingCategoryApi {
    private final SpendingCategoryUseCase spendingCategoryUseCase;

    @Override
    @PostMapping("")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> postSpendingCategory(@Validated SpendingCategoryDto.CreateParamReq param, @AuthenticationPrincipal SecurityUserDetails user) {
        if (param.icon().equals(SpendingCategory.CUSTOM)) {
            throw new SpendingErrorException(SpendingErrorCode.INVALID_ICON);
        }

        SpendingCategoryDto.Res spendingCategory = spendingCategoryUseCase.createSpendingCategory(user.getUserId(), param.name(), param.icon());
        return ResponseEntity.ok(SuccessResponse.from("spendingCategory", spendingCategory));
    }

    @Override
    @GetMapping("")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getSpendingCategories(@AuthenticationPrincipal SecurityUserDetails user) {
        return ResponseEntity.ok(SuccessResponse.from("spendingCategories", spendingCategoryUseCase.getSpendingCategories(user.getUserId())));
    }

    @Override
    @GetMapping("/{categoryId}/spendings/count")
    @PreAuthorize("isAuthenticated() and @spendingCategoryManager.hasPermission(#user.getUserId(), #categoryId, #type)")
    public ResponseEntity<?> getSpendingTotalCountByCategory(
            @PathVariable(value = "categoryId") Long categoryId,
            @RequestParam(value = "type") SpendingCategoryType type,
            @AuthenticationPrincipal SecurityUserDetails user
    ) {
        if (type.equals(SpendingCategoryType.DEFAULT) && (categoryId.equals(0L) || categoryId.equals(12L))) {
            throw new SpendingErrorException(SpendingErrorCode.INVALID_TYPE_WITH_CATEGORY_ID);
        }

        return ResponseEntity.ok(SuccessResponse.from("totalCount", spendingCategoryUseCase.getSpendingTotalCountByCategory(user.getUserId(), categoryId, type)));
    }

    @Override
    @GetMapping("/{categoryId}/spendings")
    @PreAuthorize("isAuthenticated() and @spendingCategoryManager.hasPermission(#user.getUserId(), #categoryId, #type)")
    public ResponseEntity<?> getSpendingsByCategory(
            @PathVariable(value = "categoryId") Long categoryId,
            @RequestParam(value = "type") SpendingCategoryType type,
            @PageableDefault(size = 30, page = 0) @SortDefault(sort = "spending.spendAt", direction = Sort.Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal SecurityUserDetails user
    ) {
        if (type.equals(SpendingCategoryType.DEFAULT) && (categoryId.equals(0L) || categoryId.equals(12L))) {
            throw new SpendingErrorException(SpendingErrorCode.INVALID_TYPE_WITH_CATEGORY_ID);
        }

        return ResponseEntity.ok(SuccessResponse.from("spendings", spendingCategoryUseCase.getSpendingsByCategory(user.getUserId(), categoryId, pageable, type)));
    }
}
