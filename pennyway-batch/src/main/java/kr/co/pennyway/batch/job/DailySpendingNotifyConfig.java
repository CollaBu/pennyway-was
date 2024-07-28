package kr.co.pennyway.batch.job;

import kr.co.pennyway.batch.common.dto.DeviceTokenOwner;
import kr.co.pennyway.batch.reader.ActiveDeviceTokenReader;
import kr.co.pennyway.batch.writer.DailySpendingNotifyWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class DailySpendingNotifyConfig {
    private final JobRepository jobRepository;
    private final ActiveDeviceTokenReader reader;
    private final DailySpendingNotifyWriter writer;

    @Bean
    public Job dailyNotificationJob(PlatformTransactionManager transactionManager) {
        return new JobBuilder("dailyNotificationJob", jobRepository)
                .start(dailyNotificationStep(transactionManager))
                .on("FAILED")
                .stopAndRestart(dailyNotificationStep(transactionManager))
                .on("*")
                .end()
                .end()
                .build();
    }

    @Bean
    @JobScope
    public Step dailyNotificationStep(PlatformTransactionManager transactionManager) {
        return new StepBuilder("sendSpendingNotifyStep", jobRepository)
                .<DeviceTokenOwner, DeviceTokenOwner>chunk(1000, transactionManager)
                .reader(reader.querydslNoOffsetPagingItemReader())
                .writer(writer)
                .build();
    }
}
