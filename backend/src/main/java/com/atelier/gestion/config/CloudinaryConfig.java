package com.atelier.gestion.config;

import com.cloudinary.Cloudinary;
import java.util.Map;
import org.springframework.context.annotation.*;

@Configuration
public class CloudinaryConfig {
  @Bean Cloudinary cloudinary(AtelierProperties p) {
    return new Cloudinary(Map.of("cloud_name", p.cloudinary().cloudName(), "api_key",
        p.cloudinary().apiKey(), "api_secret", p.cloudinary().apiSecret(), "secure", true));
  }
}
