package kr.co.pennyway.batch.reader;

import kr.co.pennyway.domain.domains.device.domain.DeviceToken;
import kr.co.pennyway.domain.domains.device.repository.DeviceTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
@RequiredArgsConstructor
public class ActiveDeviceTokenReader {
    private final DeviceTokenRepository deviceTokenRepository;

    @Bean
    public RepositoryItemReader<DeviceToken> activeDeviceTokenReader() {
        return new RepositoryItemReaderBuilder<DeviceToken>()
                .name("activeDeviceTokenReader")
                .repository(deviceTokenRepository)
                .methodName("findByActivatedIsTrue")
                .pageSize(100)
                .sorts(new HashMap<String, Sort.Direction>() {{
                    put("id", Sort.Direction.ASC);
                }})
                .build();
    }
}
