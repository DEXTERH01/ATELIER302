package com.atelier.gestion.config;

import java.time.Clock;
import org.springframework.context.annotation.*;

@Configuration
public class TimeConfig {
  @Bean Clock clock(){return Clock.systemUTC();}
}
