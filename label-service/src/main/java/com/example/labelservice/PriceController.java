package com.example.labelservice;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Minimal price API: a central system sets product prices,
 * label simulators (shelf labels) fetch and "display" them.
 * State is in-memory on purpose (stateless demo, no DB yet).
 */
@RestController
@RequestMapping("/api/prices")
public class PriceController {

    public record PriceRequest(BigDecimal price, String currency) {}

    public record Price(String productId, BigDecimal price, String currency, Instant updatedAt) {}

    private final Map<String, Price> prices = new ConcurrentHashMap<>();

    @PutMapping("/{productId}")
    public Price setPrice(@PathVariable String productId, @RequestBody PriceRequest request) {
        Price price = new Price(productId, request.price(),
                request.currency() == null ? "EUR" : request.currency(), Instant.now());
        prices.put(productId, price);
        return price;
    }

    @GetMapping("/{productId}")
    public ResponseEntity<Price> getPrice(@PathVariable String productId) {
        Price price = prices.get(productId);
        return price == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(price);
    }
}
