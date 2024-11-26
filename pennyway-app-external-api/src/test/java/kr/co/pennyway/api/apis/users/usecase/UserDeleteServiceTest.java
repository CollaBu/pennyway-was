package kr.co.pennyway.api.apis.users.usecase;

import kr.co.pennyway.api.apis.users.service.UserDeleteService;
import kr.co.pennyway.api.config.ExternalApiDBTestConfig;
import kr.co.pennyway.api.config.TestJpaConfig;
import kr.co.pennyway.api.config.fixture.*;
import kr.co.pennyway.domain.config.JpaConfig;
import kr.co.pennyway.domain.context.account.service.DeviceTokenService;
import kr.co.pennyway.domain.context.account.service.OauthService;
import kr.co.pennyway.domain.context.account.service.UserService;
import kr.co.pennyway.domain.context.chat.service.ChatMemberService;
import kr.co.pennyway.domain.context.finance.service.SpendingCategoryService;
import kr.co.pennyway.domain.context.finance.service.SpendingService;
import kr.co.pennyway.domain.domains.chatroom.domain.ChatRoom;
import kr.co.pennyway.domain.domains.chatroom.repository.ChatRoomRepository;
import kr.co.pennyway.domain.domains.device.domain.DeviceToken;
import kr.co.pennyway.domain.domains.member.repository.ChatMemberRepository;
import kr.co.pennyway.domain.domains.oauth.domain.Oauth;
import kr.co.pennyway.domain.domains.oauth.type.Provider;
import kr.co.pennyway.domain.domains.spending.domain.Spending;
import kr.co.pennyway.domain.domains.spending.domain.SpendingCustomCategory;
import kr.co.pennyway.domain.domains.user.domain.User;
import kr.co.pennyway.domain.domains.user.exception.UserErrorCode;
import kr.co.pennyway.domain.domains.user.exception.UserErrorException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.util.AssertionErrors.*;

@Slf4j
@ExtendWith(MockitoExtension.class)
@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=create")
@ContextConfiguration(classes = {JpaConfig.class, UserDeleteService.class, UserService.class, OauthService.class, DeviceTokenService.class, SpendingService.class, SpendingCategoryService.class, ChatMemberService.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestJpaConfig.class)
public class UserDeleteServiceTest extends ExternalApiDBTestConfig {
    @Autowired
    private UserService userService;

    @Autowired
    private OauthService oauthService;

    @Autowired
    private DeviceTokenService deviceTokenService;

    @Autowired
    private UserDeleteService userDeleteService;

    @Autowired
    private SpendingService spendingService;

    @Autowired
    private SpendingCategoryService spendingCategoryService;

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    @Autowired
    private ChatMemberRepository chatMemberRepository;

    @Test
    @Transactional
    @DisplayName("사용자가 삭제된 유저를 조회하려는 경우 NOT_FOUND 에러를 반환한다.")
    void deleteAccountWhenUserIsDeleted() {
        // given
        User user = userService.createUser(UserFixture.GENERAL_USER.toUser());
        userService.deleteUser(user);

        // when - then
        UserErrorException ex = assertThrows(UserErrorException.class, () -> userDeleteService.execute(user.getId()));
        assertEquals("삭제된 사용자인 경우 Not Found를 반환한다.", UserErrorCode.NOT_FOUND, ex.getBaseErrorCode());
    }

    @Test
    @Transactional
    @DisplayName("일반 회원가입 이력만 있는 사용자의 경우, 정상적으로 계정이 삭제된다.")
    void deleteAccount() {
        // given
        User user = UserFixture.GENERAL_USER.toUser();
        userService.createUser(user);

        // when - then
        assertDoesNotThrow(() -> userDeleteService.execute(user.getId()));
        assertTrue("사용자가 삭제되어 있어야 한다.", userService.readUser(user.getId()).isEmpty());
    }

    @Test
    @Transactional
    @DisplayName("사용자 계정 삭제 시, 연동된 모든 소셜 계정은 soft delete 처리되어야 한다.")
    void deleteAccountWithSocialAccounts() {
        // given
        User user = UserFixture.OAUTH_USER.toUser();
        userService.createUser(user);

        Oauth kakao = createOauth(Provider.KAKAO, "kakaoId", user);
        Oauth google = createOauth(Provider.GOOGLE, "googleId", user);

        // when - then
        assertDoesNotThrow(() -> userDeleteService.execute(user.getId()));

        assertTrue("사용자가 삭제되어 있어야 한다.", userService.readUser(user.getId()).isEmpty());
        assertTrue("카카오 계정이 삭제되어 있어야 한다.", oauthService.readOauth(kakao.getId()).get().isDeleted());
        assertTrue("구글 계정이 삭제되어 있어야 한다.", oauthService.readOauth(google.getId()).get().isDeleted());
    }

    @Test
    @Transactional
    @DisplayName("사용자 삭제 시, 디바이스 정보는 비활성화되어야 한다.")
    void deleteAccountWithDevices() {
        // given
        User user = UserFixture.GENERAL_USER.toUser();
        userService.createUser(user);

        DeviceToken deviceToken = DeviceTokenFixture.INIT.toDevice(user);
        deviceTokenService.createDeviceToken(deviceToken);

        // when - then
        assertDoesNotThrow(() -> userDeleteService.execute(user.getId()));
        assertTrue("사용자가 삭제되어 있어야 한다.", userService.readUser(user.getId()).isEmpty());
        assertFalse("디바이스가 비활성화 있어야 한다.", deviceTokenService.readDeviceTokenByUserIdAndToken(user.getId(), deviceToken.getToken()).get().getActivated());
    }

    @Test
    @Transactional
    @DisplayName("사용자가 등록한 지출 정보는 삭제되어야 한다.")
    void deleteAccountWithSpending() {
        // given
        User user = userService.createUser(UserFixture.GENERAL_USER.toUser());

        SpendingCustomCategory category = spendingCategoryService.createSpendingCustomCategory(SpendingCustomCategoryFixture.GENERAL_SPENDING_CUSTOM_CATEGORY.toCustomSpendingCategory(user));

        Spending spending1 = spendingService.createSpending(SpendingFixture.GENERAL_SPENDING.toSpending(user));
        Spending spending2 = spendingService.createSpending(SpendingFixture.CUSTOM_CATEGORY_SPENDING.toCustomCategorySpending(user, category));
        Spending spending3 = spendingService.createSpending(SpendingFixture.MAX_SPENDING.toSpending(user));

        // when - then
        assertDoesNotThrow(() -> userDeleteService.execute(user.getId()));
        assertTrue("사용자가 삭제되어 있어야 한다.", userService.readUser(user.getId()).isEmpty());
        assertTrue("지출 정보가 삭제되어 있어야 한다.", spendingService.readSpendings(user.getId(), spending1.getSpendAt().getYear(), spending1.getSpendAt().getMonthValue()).isEmpty());
        assertTrue("지출 카테고리가 삭제되어 있어야 한다.", spendingCategoryService.readSpendingCustomCategory(category.getId()).isEmpty());
    }

    @Test
    @Transactional
    @DisplayName("사용자가 채팅방장으로 등록된 채팅방이 하나 이상 존재하는 경우, 삭제할 수 없다.")
    void deleteAccountWithOwnershipChatRoom() {
        // given
        User user = userService.createUser(UserFixture.GENERAL_USER.toUser());
        ChatRoom chatRoom = chatRoomRepository.save(ChatRoomFixture.PUBLIC_CHAT_ROOM.toEntity(1L));
        chatMemberRepository.save(ChatMemberFixture.ADMIN.toEntity(user, chatRoom));

        // when - then
        UserErrorException ex = assertThrows(UserErrorException.class, () -> userDeleteService.execute(user.getId()));
        assertEquals("채팅방장으로 등록된 채팅방이 하나 이상 존재하는 경우, 삭제할 수 없다.", UserErrorCode.HAS_OWNERSHIP_CHAT_ROOM, ex.getBaseErrorCode());
    }

    private Oauth createOauth(Provider provider, String providerId, User user) {
        Oauth oauth = Oauth.of(provider, providerId, user);
        return oauthService.createOauth(oauth);
    }
}
