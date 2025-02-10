package kr.co.pennyway.batch.job;

import kr.co.pennyway.batch.common.dto.KeyValue;
import kr.co.pennyway.batch.processor.LastMessageIdProcessor;
import kr.co.pennyway.batch.reader.LastMessageIdReader;
import kr.co.pennyway.batch.writer.LastMessageIdWriter;
import kr.co.pennyway.domain.domains.chatstatus.domain.ChatMessageStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class LastMessageIdJobConfig {
    private static final int CHUNK_SIZE = 1000;
    private static final String PREFIX_PATTERN = "chat:last_read:*";
    private final JobRepository jobRepository;
    private final LastMessageIdProcessor processor;
    private final LastMessageIdWriter writer;
    private final RedisTemplate<String, String> redisTemplate;

    @Bean
    public Job lastMessageIdJob(PlatformTransactionManager transactionManager) {
        return new JobBuilder("lastMessageIdJob", jobRepository)
                .start(lastMessageIdStep(transactionManager))
                .on("FAILED")
                .stopAndRestart(lastMessageIdStep(transactionManager))
                .on("*")
                .end()
                .end()
                .build();
    }

    @Bean
    @JobScope
    public Step lastMessageIdStep(PlatformTransactionManager transactionManager) {
        return new StepBuilder("lastMessageIdStep", jobRepository)
                .<KeyValue, ChatMessageStatus>chunk(CHUNK_SIZE, transactionManager)
                .reader(lastMessageIdReader())
                .processor(processor)
                .writer(writer)
                .build();
    }

    @Bean
    @StepScope
    public LastMessageIdReader lastMessageIdReader() {
        ScanOptions options = ScanOptions.scanOptions().match(PREFIX_PATTERN).count(CHUNK_SIZE).build();
        Cursor<String> cursor = redisTemplate.scan(options);
        return new LastMessageIdReader(redisTemplate, cursor);
    }
}
