package com.example.labelservice;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** Shows which configuration this instance runs with (never the secret value itself). */
@RestController
public class InfoController {

    private final String environment;
    private final String defaultCurrency;
    private final boolean writeProtected;

    public InfoController(@Value("${app.environment}") String environment,
                          @Value("${app.default-currency}") String defaultCurrency,
                          @Value("${app.api-key}") String apiKey) {
        this.environment = environment;
        this.defaultCurrency = defaultCurrency;
        this.writeProtected = !apiKey.isBlank();
    }

    @GetMapping("/api/info")
    public Map<String, Object> info() {
        // HOSTNAME is the Pod name in Kubernetes: shows which replica answered
        return Map.of(
                "environment", environment,
                "defaultCurrency", defaultCurrency,
                "writeProtected", writeProtected,
                "pod", System.getenv().getOrDefault("HOSTNAME", "local"));
    }
}
