package kr.co.pennyway.api.apis.users.usecase;

import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.co.pennyway.api.apis.users.service.UserProfileUpdateService;
import kr.co.pennyway.api.config.ExternalApiDBTestConfig;
import kr.co.pennyway.api.config.fixture.UserFixture;
import kr.co.pennyway.domain.config.JpaConfig;
import kr.co.pennyway.domain.domains.user.domain.User;
import kr.co.pennyway.domain.domains.user.exception.UserErrorCode;
import kr.co.pennyway.domain.domains.user.exception.UserErrorException;
import kr.co.pennyway.domain.domains.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.util.AssertionErrors.assertEquals;

@ExtendWith(MockitoExtension.class)
@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=create")
@ContextConfiguration(classes = {JpaConfig.class, UserProfileUpdateService.class, UserService.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class UserProfileUpdateServiceTest extends ExternalApiDBTestConfig {
    @Autowired
    private UserService userService;

    @Autowired
    private UserProfileUpdateService userProfileUpdateService;

    @MockBean
    private JPAQueryFactory queryFactory;

    @Test
    @Transactional
    @DisplayName("사용자가 삭제된 유저인 경우 NOT_FOUND 에러를 반환한다.")
    void updateNameWhenUserIsDeleted() {
        // given
        String newName = "양재서";
        User originUser = UserFixture.GENERAL_USER.toUser();
        userService.createUser(originUser);
        userService.deleteUser(originUser);

        // when - then
        UserErrorException ex = assertThrows(UserErrorException.class, () -> userProfileUpdateService.updateName(originUser.getId(), newName));
        assertEquals("삭제된 사용자인 경우 Not Found를 반환한다.", UserErrorCode.NOT_FOUND, ex.getBaseErrorCode());
    }

    @Test
    @Transactional
    @DisplayName("사용자의 이름이 성공적으로 변경된다.")
    void updateName() {
        // given
        User originUser = UserFixture.GENERAL_USER.toUser();
        userService.createUser(originUser);
        String newName = "양재서";

        // when
        userProfileUpdateService.updateName(originUser.getId(), newName);

        // then
        User updatedUser = userService.readUser(originUser.getId()).orElseThrow();
        assertEquals("사용자 이름이 변경되어 있어야 한다.", newName, updatedUser.getName());
    }
}
