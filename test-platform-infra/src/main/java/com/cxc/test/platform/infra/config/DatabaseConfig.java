package com.cxc.test.platform.infra.config;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DatabaseConfig {

    private String driverClassName;

    private String url;

    private String name;

    private String pwd;
}
