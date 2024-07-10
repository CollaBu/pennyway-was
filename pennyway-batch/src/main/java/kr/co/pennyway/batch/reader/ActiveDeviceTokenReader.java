package kr.co.pennyway.batch.reader;

import kr.co.pennyway.domain.domains.device.dto.DeviceTokenOwner;
import kr.co.pennyway.domain.domains.device.repository.DeviceTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class ActiveDeviceTokenReader {
    private final DeviceTokenRepository deviceTokenRepository;

    @Bean
    public RepositoryItemReader<DeviceTokenOwner> execute() {
        log.info("ActiveDeviceToken 읽기 실행");

        return new RepositoryItemReaderBuilder<DeviceTokenOwner>()
                .name("execute")
                .repository(deviceTokenRepository)
                .methodName("findActivatedDeviceTokenOwners")
                .pageSize(100)
                .sorts(new HashMap<>() {{
                    put("id", Sort.Direction.ASC);
                }})
                .build();
    }
}
