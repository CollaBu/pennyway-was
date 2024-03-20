package kr.co.pennyway.domain.domains.user.domain;

import jakarta.persistence.Embeddable;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;

@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicInsert
@ToString(of = {"accountBookNotify", "feedNotify", "feedCommentNotify"})
public class NotifySetting {
    @ColumnDefault("true")
    private Boolean accountBookNotify;
    @ColumnDefault("true")
    private Boolean feedNotify;
    @ColumnDefault("true")
    private Boolean feedCommentNotify;

    @Builder
    private NotifySetting(Boolean accountBookNotify, Boolean feedNotify, Boolean feedCommentNotify) {
        this.accountBookNotify = accountBookNotify;
        this.feedNotify = feedNotify;
        this.feedCommentNotify = feedCommentNotify;
    }

    public static NotifySetting of(Boolean accountBookNotify, Boolean feedNotify, Boolean feedCommentNotify) {
        return NotifySetting.builder()
                .accountBookNotify(accountBookNotify)
                .feedNotify(feedNotify)
                .feedCommentNotify(feedCommentNotify)
                .build();
    }
}
