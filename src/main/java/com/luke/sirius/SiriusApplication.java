package com.luke.sirius;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.TimeZone;

@EnableScheduling
@SpringBootApplication
public class SiriusApplication {

    public static void main(String[] args) {
        TimeZone timeZone = TimeZone.getTimeZone("GMT+8:00");
        TimeZone.setDefault(timeZone);
        SpringApplication.run(SiriusApplication.class, args);
    }

}
