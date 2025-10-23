package com.lucia.memoria.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
@ConfigurationProperties(prefix = "api")
public class ExternalAPIConfig {
    private  String freeDictionary;
    private  String googleTranslate;
}
