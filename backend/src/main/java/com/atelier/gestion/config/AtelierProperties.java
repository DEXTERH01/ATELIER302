package com.atelier.gestion.config;
import java.util.Arrays;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
@ConfigurationProperties(prefix = "atelier")
public record AtelierProperties(Cors cors, Jwt jwt, Cookie cookie) {
  public record Cors(String allowedOrigins) { public List<String> origins() { return Arrays.stream(allowedOrigins.split(",")).map(String::trim).filter(s -> !s.isBlank()).toList(); } }
  public record Jwt(String secret, String issuer, long accessExpirationMinutes, long refreshExpirationDays) {}
  public record Cookie(String name, boolean secure) {}
}

