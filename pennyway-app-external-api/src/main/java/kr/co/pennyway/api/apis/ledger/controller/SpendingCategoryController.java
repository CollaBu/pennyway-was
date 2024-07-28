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
    @DeleteMapping("/{categoryId}")
    @PreAuthorize("isAuthenticated() and @spendingCategoryManager.hasPermission(principal.userId, #categoryId)")
    public ResponseEntity<?> deleteSpendingCategory(@PathVariable Long categoryId) {
        spendingCategoryUseCase.deleteSpendingCategory(categoryId);

        return ResponseEntity.ok(SuccessResponse.noContent());
    }

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

    @Override
    @PatchMapping("/{categoryId}")
    @PreAuthorize("isAuthenticated() and @spendingCategoryManager.hasPermission(principal.userId, #categoryId)")
    public ResponseEntity<?> patchSpendingCategory(@PathVariable Long categoryId, @Validated SpendingCategoryDto.CreateParamReq param) {
        if (SpendingCategory.CUSTOM.equals(param.icon())) {
            throw new SpendingErrorException(SpendingErrorCode.INVALID_ICON);
        }

        return ResponseEntity.ok(SuccessResponse.from("spendingCategory", spendingCategoryUseCase.updateSpendingCategory(categoryId, param.name(), param.icon())));
    }

    @Override
    @PatchMapping({"{fromId}/migration"})
    @PreAuthorize("isAuthenticated() and @spendingCategoryManager.hasPermission(principal.userId, #fromId, #fromType) and @spendingCategoryManager.hasPermission(principal.userId, #toId, #toType)")
    public ResponseEntity<?> migrateSpendingsByCategory(
            @PathVariable Long fromId,
            @RequestParam(value = "fromType") SpendingCategoryType fromType,
            @RequestParam(value = "toId") Long toId,
            @RequestParam(value = "toType") SpendingCategoryType toType,
            @AuthenticationPrincipal SecurityUserDetails user
    ) {
        Long userId = user.getUserId();
        spendingCategoryUseCase.migrateSpendingsByCategory(fromId, fromType, toId, toType, userId);

        return ResponseEntity.ok(SuccessResponse.noContent());
    }


}
