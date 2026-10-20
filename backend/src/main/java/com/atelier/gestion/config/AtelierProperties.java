package com.atelier.gestion.config;
import java.util.Arrays;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
@ConfigurationProperties(prefix = "atelier")
public record AtelierProperties(Cors cors, Jwt jwt, Cookie cookie, Cloudinary cloudinary, Files files, String environment) {
  public record Cors(String allowedOrigins) { public List<String> origins() { return Arrays.stream(allowedOrigins.split(",")).map(String::trim).filter(s -> !s.isBlank()).toList(); } }
  public record Jwt(String secret, String issuer, long accessExpirationMinutes, long refreshExpirationDays) {}
  public record Cookie(String name, boolean secure) {}
  public record Cloudinary(String cloudName, String apiKey, String apiSecret) {}
  public record Files(long maxUploadBytes) {}
}

