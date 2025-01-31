package kr.co.pennyway.api.apis.ledger.helper;

import kr.co.pennyway.api.apis.ledger.service.DailySpendingAggregateService;
import kr.co.pennyway.common.annotation.Helper;
import kr.co.pennyway.domain.context.account.service.UserService;
import kr.co.pennyway.domain.context.chat.service.ChatMemberService;
import kr.co.pennyway.domain.domains.user.exception.UserErrorCode;
import kr.co.pennyway.domain.domains.user.exception.UserErrorException;
import kr.co.pennyway.infra.common.event.SpendingChatShareEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Helper
@RequiredArgsConstructor
public class SpendingChatShareHelper {
    private final DailySpendingAggregateService dailySpendingAggregateService;

    private final UserService userService;
    private final ChatMemberService chatMemberService;

    private final ApplicationEventPublisher eventPublisher;

    public void execute(Long userId, List<Long> chatRoomIds, LocalDate date) {
        var user = userService.readUser(userId)
                .orElseThrow(() -> new UserErrorException(UserErrorCode.NOT_FOUND));
        var aggregatedSpendings = dailySpendingAggregateService.execute(userId, date.getYear(), date.getMonthValue(), date.getDayOfMonth());
        var joinedChatRoomIds = chatMemberService.readChatRoomIdsByUserId(userId);

        var spendingOnDate = new ArrayList<SpendingChatShareEvent.SpendingOnDate>();
        for (var pair : aggregatedSpendings) {
            var categoryInfo = pair.getFirst();
            var amount = pair.getSecond();

            spendingOnDate.add(SpendingChatShareEvent.SpendingOnDate.of(categoryInfo.id(), categoryInfo.name(), categoryInfo.icon().name(), amount));
        }

        chatRoomIds.stream()
                .filter(joinedChatRoomIds::contains)
                .forEach(chatRoomId -> {
                    eventPublisher.publishEvent(new SpendingChatShareEvent(chatRoomId, user.getName(), user.getId(), date, spendingOnDate));
                });
    }
}
