package kr.co.pennyway;

import java.util.TimeZone;

@SpringBootApplication
public class PennywaySocketApplication {
    public static void main(String[] args) {
        SpringApplication.run(PennywaySocketApplication.class, args);
    }

    @PostConstruct
    public void init() {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
    }
}
