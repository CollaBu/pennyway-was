package kr.co.pennyway;

import jakarta.annotation.PostConstruct
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import java.util.*

@SpringBootApplication
class PennywaySocketApplication

fun main(args: Array<String>) {
    runApplication<PennywaySocketApplication>(*args)
}

@PostConstruct
fun setDefaultTimeZone() {
    TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"))
}