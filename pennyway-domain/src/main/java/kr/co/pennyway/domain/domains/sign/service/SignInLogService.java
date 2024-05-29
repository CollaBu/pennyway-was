package kr.co.pennyway.domain.domains.sign.service;

import kr.co.pennyway.common.annotation.DomainService;
import kr.co.pennyway.domain.domains.sign.repository.SignInLogRepository;
import lombok.RequiredArgsConstructor;

@DomainService
@RequiredArgsConstructor
public class SignInLogService {
    private final SignInLogRepository signInLogRepository;
}
