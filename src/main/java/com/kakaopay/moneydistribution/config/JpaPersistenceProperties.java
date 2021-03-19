package com.kakaopay.moneydistribution.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "jpa-persistence")
@Getter
@Setter
public class JpaPersistenceProperties {
    private String lockTimeout;
}
