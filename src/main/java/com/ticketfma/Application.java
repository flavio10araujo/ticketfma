package com.ticketfma;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;

@SpringBootApplication
@OpenAPIDefinition(info = @Info(title = "Ticket FMA Service", contact = @Contact(email = "flavio10araujo@gmail.com")))
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
