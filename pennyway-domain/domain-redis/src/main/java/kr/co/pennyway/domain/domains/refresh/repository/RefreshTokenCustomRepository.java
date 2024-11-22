package kr.co.pennyway.domain.domains.refresh.repository;

public interface RefreshTokenCustomRepository {
    void deleteAllByUserId(Long userId);
}
