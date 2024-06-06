package kr.co.pennyway.domain.domains.user.domain;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;

@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicInsert
@ToString(of = {"accountBookNotify", "feedNotify", "chatNotify"})
public class NotifySetting {
    @ColumnDefault("true")
    private boolean accountBookNotify;
    @ColumnDefault("true")
    private boolean feedNotify;
    @ColumnDefault("true")
    private boolean chatNotify;

    @Builder
    private NotifySetting(boolean accountBookNotify, boolean feedNotify, boolean chatNotify) {
        this.accountBookNotify = accountBookNotify;
        this.feedNotify = feedNotify;
        this.chatNotify = chatNotify;
    }

    public static NotifySetting of(boolean accountBookNotify, boolean feedNotify, boolean chatNotify) {
        return NotifySetting.builder()
                .accountBookNotify(accountBookNotify)
                .feedNotify(feedNotify)
                .chatNotify(chatNotify)
                .build();
    }

    public void updateNotifySetting(NotifyType notifyType, boolean flag) {
        switch (notifyType) {
            case ACCOUNT_BOOK -> this.accountBookNotify = flag;
            case FEED -> this.feedNotify = flag;
            case CHAT -> this.chatNotify = flag;
        }
    }

    public boolean isAccountBookNotify() {
        return accountBookNotify;
    }

    public boolean isFeedNotify() {
        return feedNotify;
    }

    public boolean isChatNotify() {
        return chatNotify;
    }

    public enum NotifyType {
        ACCOUNT_BOOK, FEED, CHAT
    }
}
