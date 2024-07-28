package kr.co.pennyway.batch.job;

import kr.co.pennyway.batch.common.dto.DeviceTokenOwner;
import kr.co.pennyway.batch.reader.ActiveDeviceTokenReader;
import kr.co.pennyway.batch.writer.MonthlyTotalAmountNotifyWriter;
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
public class MonthlyTargetAmountNotifyConfig {
    private final JobRepository jobRepository;
    private final ActiveDeviceTokenReader reader;
    private final MonthlyTotalAmountNotifyWriter writer;

    @Bean
    public Job monthlyNotificationJob(PlatformTransactionManager transactionManager) {
        return new JobBuilder("monthlyNotificationJob", jobRepository)
                .start(monthlyNotificationStep(transactionManager))
                .on("FAILED")
                .stopAndRestart(monthlyNotificationStep(transactionManager))
                .on("*")
                .end()
                .end()
                .build();
    }

    @Bean
    @JobScope
    public Step monthlyNotificationStep(PlatformTransactionManager transactionManager) {
        return new StepBuilder("sendMonthlyNotifyStep", jobRepository)
                .<DeviceTokenOwner, DeviceTokenOwner>chunk(1000, transactionManager)
                .reader(reader.querydslNoOffsetPagingItemReader())
                .writer(writer)
                .build();
    }
}
