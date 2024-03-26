package kr.co.pennyway.domain.domains.user.domain;

import jakarta.persistence.Embeddable;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;

@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicInsert
@ToString(of = {"accountBookNotify", "feedNotify", "chatNotify"})
public class NotifySetting {
    @ColumnDefault("true")
    private Boolean accountBookNotify;
    @ColumnDefault("true")
    private Boolean feedNotify;
    @ColumnDefault("true")
    private Boolean chatNotify;

    @Builder
    private NotifySetting(Boolean accountBookNotify, Boolean feedNotify, Boolean chatNotify) {
        this.accountBookNotify = accountBookNotify;
        this.feedNotify = feedNotify;
        this.chatNotify = chatNotify;
    }

    public static NotifySetting of(Boolean accountBookNotify, Boolean feedNotify, Boolean chatNotify) {
        return NotifySetting.builder()
                .accountBookNotify(accountBookNotify)
                .feedNotify(feedNotify)
                .chatNotify(chatNotify)
                .build();
    }
}
