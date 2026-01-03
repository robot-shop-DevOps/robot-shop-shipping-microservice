package com.robotshop.shipping.controller;

import java.sql.Connection;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.robotshop.shipping.model.City;
import com.robotshop.shipping.model.Code;
import com.robotshop.shipping.model.Ship;
import com.robotshop.shipping.repository.CityRepository;
import com.robotshop.shipping.repository.CodeRepository;
import com.robotshop.shipping.service.Calculator;
import com.robotshop.shipping.service.CartHelper;

@RestController
public class Controller {

    private static final Logger logger =
            LoggerFactory.getLogger(Controller.class);

    public static List<byte[]> bytesGlobal =
            Collections.synchronizedList(new ArrayList<>());

    private final String cartUrl;
    private final DataSource dataSource;

    @Autowired
    private CityRepository cityrepo;

    @Autowired
    private CodeRepository coderepo;

    public Controller(
        @Value("${cart.endpoint}") String cartEndpoint,
        @Value("${cart.port}") String cartPort,
        DataSource dataSource
    ) {
        this.cartUrl   = "http://" + cartEndpoint + ":" + cartPort + "/shipping/";
        this.dataSource = dataSource;
    }

    // ---------------- HEALTH ----------------

    @GetMapping("/health/liveness")
    public ResponseEntity<String> live() {
        return ResponseEntity.ok("OK");
    }

    @GetMapping("/health/readiness")
    public ResponseEntity<String> ready() {
        try (Connection conn = dataSource.getConnection()) {
            return ResponseEntity.ok("OK");
        } catch (Exception e) {
            logger.error(
                "database unavailable",
                e
            );
            return ResponseEntity.status(503).body("DB unavailable");
        }
    }

    // ---------------- MEMORY (TEST / DEMO) ----------------

    @GetMapping("/memory")
    public int memory() {
        logger.info("Allocating memory block (25MB)");

        byte[] bytes = new byte[1024 * 1024 * 25];
        Arrays.fill(bytes, (byte) 8);
        bytesGlobal.add(bytes);

        logger.info("Memory blocks allocated: {}", bytesGlobal.size());
        return bytesGlobal.size();
    }

    @GetMapping("/free")
    public int free() {
        logger.info("Clearing allocated memory");

        bytesGlobal.clear();

        logger.info("Memory cleared");
        return bytesGlobal.size();
    }

    // ---------------- DATA ----------------

    @GetMapping("/count")
    public String count() {
        return String.valueOf(cityrepo.count());
    }

    @GetMapping("/codes")
    public Iterable<Code> codes() {
        return coderepo.findAll(Sort.by(Sort.Direction.ASC, "name"));
    }

    @GetMapping("/cities/{code}")
    public List<City> cities(@PathVariable String code) {
        return cityrepo.findByCode(code);
    }

    @GetMapping("/match/{code}/{text}")
    public List<City> match(@PathVariable String code, @PathVariable String text) {
        if (text.length() < 3) {
            logger.warn(
                "invalid match query",
                org.slf4j.MarkerFactory.getMarker("VALIDATION")
            );
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        List<City> cities = cityrepo.match(code, text);
        if (cities.size() > 10) {
            return cities.subList(0, 9);
        }
        return cities;
    }

    // ---------------- SHIPPING ----------------

    @GetMapping("/calc/{id}")
    public Ship calc(@PathVariable long id) {
        City city = cityrepo.findById(id).orElse(null);
        if (city == null) {
            logger.warn(
                "city not found",
                org.slf4j.MarkerFactory.getMarker("NOT_FOUND")
            );
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "city not found");
        }

        Calculator calc = new Calculator(city);
        long distance = calc.getDistance(51.164896, 7.068792);
        double cost = Math.rint(distance * 5) / 100.0;

        return new Ship(distance, cost);
    }

    // ---------------- CONFIRM ----------------

    @PostMapping(
        path = "/confirm/{id}",
        consumes = "application/json",
        produces = "application/json"
    )
    public String confirm(
        @PathVariable String id,
        @RequestBody String body,
        @RequestHeader("Authorization") String authHeader
    ) {
        try {
            CartHelper helper = new CartHelper(cartUrl, authHeader);
            String cart = helper.addToCart(id, body);

            if (cart.isEmpty()) {
                logger.warn(
                    "cart not found",
                    org.slf4j.MarkerFactory.getMarker("NOT_FOUND")
                );
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "cart not found");
            }

            return cart;

        } catch (ResponseStatusException e) {
            throw e;

        } catch (Exception e) {
            logger.error(
                "failed to confirm shipping",
                e
            );
            throw new ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "shipping confirmation failed"
            );
        }
    }
}