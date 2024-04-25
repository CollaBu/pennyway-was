package kr.co.pennyway.api.apis.users.service;

import kr.co.pennyway.domain.domains.user.domain.NotifySetting;
import kr.co.pennyway.domain.domains.user.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserProfileUpdateService {
    @Transactional
    public void updateNotifySetting(User user, NotifySetting.NotifyType type, Boolean flag) {
        user.getNotifySetting().updateNotifySetting(type, flag);
    }
}
