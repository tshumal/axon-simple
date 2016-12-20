package org.athend.axon.simple;

import org.athend.axon.simple.service.abstr.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;

import java.util.UUID;

@SpringBootApplication
@EntityScan(basePackages = {"org.axonframework.eventsourcing.eventstore.jpa", "org.axonframework.eventsourcing.eventstore.jdbc", "org.axonframework.eventhandling.saga.repository.jpa","org.axonframework.eventhandling.saga.repository.jdbc"})
public class Application {

    public static void main(final String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    CommandLineRunner init(final UserService userService) {

        return arg0 -> {
            UUID userId = userService.createUser("Some User Name");
            // sleep to emulate a continuously running app
            //Thread.sleep(5000);
           userService.lockUser(userId);

        };
    }
}
