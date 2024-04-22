package kr.co.pennyway.api.apis.auth.usecase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class UserAuthUseCaseUnitTest {
    private UserAuthUseCase userAuthUseCase;

    @BeforeEach
    public void setUp() {
        userAuthUseCase = new UserAuthUseCase();
    }
}
