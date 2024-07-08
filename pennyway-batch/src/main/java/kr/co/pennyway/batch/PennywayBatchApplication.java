package kr.co.pennyway.batch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PennywayBatchApplication {
    public static void main(String[] args) {
        SpringApplication.run(PennywayBatchApplication.class, args);
    }
}
