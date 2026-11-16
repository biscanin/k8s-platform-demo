package com.example.labelservice;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

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
    private final String defaultCurrency;
    private final String apiKey;

    public PriceController(@Value("${app.default-currency}") String defaultCurrency,
                           @Value("${app.api-key}") String apiKey) {
        this.defaultCurrency = defaultCurrency;
        this.apiKey = apiKey;
    }

    @PutMapping("/{productId}")
    public Price setPrice(@PathVariable String productId, @RequestBody PriceRequest request,
                          @RequestHeader(value = "X-Api-Key", required = false) String providedKey) {
        // The key comes from a Kubernetes Secret. Empty key = unprotected (local dev only).
        if (!apiKey.isBlank() && !apiKey.equals(providedKey)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "missing or wrong X-Api-Key");
        }
        Price price = new Price(productId, request.price(),
                request.currency() == null ? defaultCurrency : request.currency(), Instant.now());
        prices.put(productId, price);
        return price;
    }

    @GetMapping("/{productId}")
    public ResponseEntity<Price> getPrice(@PathVariable String productId) {
        Price price = prices.get(productId);
        return price == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(price);
    }
}
