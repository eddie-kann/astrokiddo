package com.astrokiddo.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.ZoneId;

@Configuration
@ConfigurationProperties(prefix = "app")
@Getter
@Setter
public class AppProperties {

    private String timeZone = "UTC";

    public ZoneId getZoneId() {
        return ZoneId.of(timeZone);
    }
}
