package kr.co.pennyway.api.apis.ledge.controller;

import kr.co.pennyway.api.apis.ledge.api.TargetAmountApi;
import kr.co.pennyway.api.apis.ledge.dto.TargetAmountDto;
import kr.co.pennyway.api.apis.ledge.usecase.TargetAmountUseCase;
import kr.co.pennyway.api.common.response.SuccessResponse;
import kr.co.pennyway.api.common.security.authentication.SecurityUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v2/targets")
public class TargetAmountController implements TargetAmountApi {
    private final TargetAmountUseCase targetAmountUseCase;

    @Override
    @PutMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> putTargetAmount(
            @Validated TargetAmountDto.UpdateParamReq request,
            @AuthenticationPrincipal SecurityUserDetails user
    ) {
        targetAmountUseCase.updateTargetAmount(user.getUserId(), request.date(), request.amount());
        return ResponseEntity.ok(SuccessResponse.noContent());
    }
}
