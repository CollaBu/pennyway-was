package kr.co.pennyway.api.apis.ledger.controller;

import kr.co.pennyway.api.apis.ledger.api.SpendingCategoryApi;
import kr.co.pennyway.api.apis.ledger.dto.SpendingCategoryDto;
import kr.co.pennyway.api.apis.ledger.usecase.SpendingCategoryUseCase;
import kr.co.pennyway.api.common.response.SuccessResponse;
import kr.co.pennyway.api.common.security.authentication.SecurityUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
        SpendingCategoryDto.Res spendingCategory = spendingCategoryUseCase.createSpendingCategory(user.getUserId(), param.name(), param.icon());
        return ResponseEntity.ok(SuccessResponse.from("spendingCategory", spendingCategory));
    }
}
