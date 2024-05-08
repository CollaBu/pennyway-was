package kr.co.pennyway.api.apis.ledge.usecase;

import kr.co.pennyway.common.annotation.UseCase;
import kr.co.pennyway.domain.domains.target.domain.TargetAmount;
import kr.co.pennyway.domain.domains.target.service.TargetAmountService;
import kr.co.pennyway.domain.domains.user.domain.User;
import kr.co.pennyway.domain.domains.user.exception.UserErrorCode;
import kr.co.pennyway.domain.domains.user.exception.UserErrorException;
import kr.co.pennyway.domain.domains.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

@Slf4j
@UseCase
@RequiredArgsConstructor
public class TargetAmountUseCase {
    private final UserService userService;
    private final TargetAmountService targetAmountService;

    @Transactional
    public void updateTargetAmount(Long userId, LocalDate date, Integer amount) {
        Optional<TargetAmount> targetAmount = targetAmountService.readTargetAmountThatMonth(userId, date);

        if (targetAmount.isPresent()) {
            targetAmount.get().updateAmount(amount);
        } else {
            User user = userService.readUser(userId).orElseThrow(() -> new UserErrorException(UserErrorCode.NOT_FOUND));
            targetAmountService.createTargetAmount(TargetAmount.of(amount, user));
        }
    }
}
