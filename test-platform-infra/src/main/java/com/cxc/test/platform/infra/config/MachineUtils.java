package com.cxc.test.platform.infra.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix ="machine")
public class MachineUtils {

    private List<String> ips;
}
