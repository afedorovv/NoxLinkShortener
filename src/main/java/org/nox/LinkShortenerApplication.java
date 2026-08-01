package org.nox;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import static org.springframework.web.servlet.function.RequestPredicates.path;
import static org.springframework.web.servlet.function.RouterFunctions.route;

@SpringBootApplication
public class LinkShortenerApplication {
    public static void main(String[] args) {
        SpringApplication.run(LinkShortenerApplication.class, args);
    }

    @Bean
    RouterFunction<ServerResponse> spaRouter() {
        ClassPathResource index = new ClassPathResource("static/index.html");
        return route().resource(path("/"), index).build();
    }
}
