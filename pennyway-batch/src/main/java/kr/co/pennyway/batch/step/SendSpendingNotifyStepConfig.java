package kr.co.pennyway.batch.step;

import kr.co.pennyway.batch.processor.NotificationProcessor;
import kr.co.pennyway.batch.reader.ActiveDeviceTokenReader;
import kr.co.pennyway.batch.writer.NotificationWriter;
import kr.co.pennyway.domain.domains.device.dto.DeviceTokenOwner;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class SendSpendingNotifyStepConfig {
    private final JobRepository jobRepository;
    private final ActiveDeviceTokenReader reader;
    private final NotificationProcessor processor;
    private final NotificationWriter writer;

    @Bean
    public Step sendSpendingNotifyStep(PlatformTransactionManager transactionManager) {
        return new StepBuilder("sendSpendingNotifyStep", jobRepository)
                .<DeviceTokenOwner, DeviceTokenOwner>chunk(100, transactionManager)
                .reader(reader.execute())
                .processor(processor)
                .writer(writer)
                .build();
    }
}
