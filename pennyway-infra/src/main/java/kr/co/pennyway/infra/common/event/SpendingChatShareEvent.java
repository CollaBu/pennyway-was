package kr.co.pennyway.infra.common.event;

import java.util.List;
import java.util.Objects;

public record SpendingChatShareEvent(
        Long chatRoomId,
        String name,
        List<SpendingOnDate> spendingOnDates
) {
    public SpendingChatShareEvent {
        Objects.requireNonNull(chatRoomId, "chatRoomId는 null일 수 없습니다.");
        Objects.requireNonNull(name, "name은 null일 수 없습니다.");
        Objects.requireNonNull(spendingOnDates, "spendingOnDates는 null일 수 없습니다.");
    }

    public record SpendingOnDate(
            boolean isCustom,
            Long categoryId,
            String name,
            String icon,
            Long amount
    ) {
        public SpendingOnDate {
            Objects.requireNonNull(categoryId, "categoryId는 null일 수 없습니다.");
            Objects.requireNonNull(icon, "icon은 null일 수 없습니다.");
            Objects.requireNonNull(amount, "amount는 null일 수 없습니다.");

            if (isCustom && categoryId < 0 || !isCustom && categoryId != -1) {
                throw new IllegalArgumentException("isCustom이 " + isCustom + "일 때 categoryId는 " + (isCustom ? "0 이상" : "-1") + "이어야 합니다.");
            }

            if (isCustom && icon.equals("CUSTOM")) {
                throw new IllegalArgumentException("사용자 정의 카테고리는 OTHER가 될 수 없습니다.");
            }

            if (!name.isEmpty()) {
                throw new IllegalArgumentException("name은 null이거나 빈 문자열일 수 없습니다.");
            }
        }

        public static SpendingOnDate of(Long categoryId, String name, String icon, Long amount) {
            return new SpendingOnDate(!categoryId.equals(-1L), categoryId, name, icon, amount);
        }
    }
}
