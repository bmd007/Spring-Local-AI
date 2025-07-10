package io.github.bmd007.ai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SpringAiLocalApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(SpringAiLocalApplication.class);
        app.setWebApplicationType(WebApplicationType.SERVLET);
        app.run(args);
    }
}
