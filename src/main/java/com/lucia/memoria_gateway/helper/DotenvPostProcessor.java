package com.lucia.memoria_gateway.helper;

import io.github.cdimascio.dotenv.Dotenv;
import java.util.Arrays;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class DotenvPostProcessor implements EnvironmentPostProcessor {

  @Override
  public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
    Map<String, Object> props = new HashMap<>();

    // Determine which env file to load
    String[] activeProfiles = environment.getActiveProfiles();
    boolean isDev = Arrays.stream(activeProfiles)
        .anyMatch(p -> p.equalsIgnoreCase("dev"));

    // Load dev.env or .env depending on profile
    Dotenv dotenv = Dotenv.configure()
        .directory("../")
        .filename(isDev ? ".env.gateway.dev" : ".env.gateway")
        .ignoreIfMissing()
        .load();

    dotenv.entries().forEach(entry -> props.put(entry.getKey(), entry.getValue()));

    // Inject into Spring's environment
    environment.getPropertySources().addFirst(
        new MapPropertySource("dotenv", props)
    );
  }
}