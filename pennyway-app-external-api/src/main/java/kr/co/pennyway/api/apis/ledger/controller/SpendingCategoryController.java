package kr.co.pennyway.api.apis.ledger.controller;

import kr.co.pennyway.api.apis.ledger.api.SpendingCategoryApi;
import kr.co.pennyway.api.apis.ledger.dto.SpendingCategoryDto;
import kr.co.pennyway.api.apis.ledger.usecase.SpendingCategoryUseCase;
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
}
