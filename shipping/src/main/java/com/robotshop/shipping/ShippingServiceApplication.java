package com.robotshop.shipping;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@SpringBootApplication
@EnableWebMvc
public class ShippingServiceApplication {

    private static final Logger logger =
            LoggerFactory.getLogger(ShippingServiceApplication.class);

    public static void main(String[] args) {
        try {
            SpringApplication.run(ShippingServiceApplication.class, args);

            logger.info(
                "shipping service started",
                org.slf4j.MarkerFactory.getMarker("STARTUP")
            );

        } catch (Exception e) {
            logger.error(
                "shipping service failed to start",
                e
            );
            throw e;
        }
    }
}