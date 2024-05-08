package kr.co.pennyway.api.apis.ledge.controller;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import jakarta.validation.constraints.Min;
import kr.co.pennyway.api.apis.ledge.api.TargetAmountApi;
import kr.co.pennyway.api.apis.ledge.usecase.TargetAmountUseCase;
import kr.co.pennyway.api.common.response.SuccessResponse;
import kr.co.pennyway.api.common.security.authentication.SecurityUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

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
            @RequestParam("date") @JsonSerialize(using = LocalDateSerializer.class) @JsonFormat(pattern = "yyyy-MM-dd") LocalDate date,
            @RequestParam("amount") @Min(value = 0, message = "amount 값은 0 이상이어야 합니다.") Integer amount,
            @AuthenticationPrincipal SecurityUserDetails user
    ) {
        targetAmountUseCase.updateTargetAmount(user.getUserId(), date, amount);
        return ResponseEntity.ok(SuccessResponse.noContent());
    }
}
