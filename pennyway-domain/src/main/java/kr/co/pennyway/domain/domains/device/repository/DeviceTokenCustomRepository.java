package kr.co.pennyway.domain.domains.device.repository;

import kr.co.pennyway.domain.domains.device.dto.DeviceTokenOwner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DeviceTokenCustomRepository {
    /**
     * 사용자 아이디, 이름 그리고 디바이스 토큰 리스트를 조회하여, {@link DeviceTokenOwner} 객체로 반환한다.
     * <p>
     * 이 때, 사용자의 계좌북 알림 설정이 활성화되어 있어야 하며, 디바이스 토큰은 활성화되어 있어야 한다.
     * <p>
     *
     * @apiNote 이 메서드는 페이징 처리를 하고 있으며, 사용자 아이디를 기준으로 오름차순 정렬한다.
     * 이 때, size가 100이고 한 명의 사용자가 여러 개의 디바이스 토큰(각각 pk가 99, 100, 101)을 가지고 있다면,
     * 101번에 대한 토큰은 다음 페이지로 넘어가게 되므로 이에 대한 예외 처리가 필요하다.
     *
     * <pre>
     * {@code
     *      SELECT d.token, u.id, u.name
     *      FROM device_token d
     *      LEFT JOIN user u ON d.user_id = u.id
     *      WHERE d.activated = true AND u.account_book_notify = true
     *      ORDER BY u.id ASC
     *      LIMIT ${pageable.pageSize} OFFSET ${pageable.offset}
     *      ;
     * }
     * </pre>
     */
    Page<DeviceTokenOwner> findActivatedDeviceTokenOwners(Pageable pageable);
}
