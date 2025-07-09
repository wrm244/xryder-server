package cn.xryder.base;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author wrm244
 */
@SpringBootApplication
@EnableScheduling
public class XryderServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(XryderServerApplication.class, args);
    }
}
