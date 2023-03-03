package com.cxc.test.platform.infra.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Slf4j
@Component
public class DbConfig {

    // 切换为上线临时公网访问链接地址
    private final String TEMP_PRE_HOST = "rm-wz9536ows19zzvnsfeo.mysql.rds.aliyuncs.com";

    public DatabaseConfig getDemoDatabaseConfig(String env, String bg) {
        if ("test".equalsIgnoreCase(env)) {
            return DatabaseConfig.builder()
                .driverClassName("com.mysql.cj.jdbc.Driver")
                .url("jdbc:mysql://mysql-0.mysql.bbmallun-middleware.svc.cluster.local:3306/oneid_test?characterEncoding=UTF-8&useLocalSessionState=true&serverTimezone=GMT%2b8")
                .name("oneid_test")
                .pwd("oneid_test")
                .build();
        } else if ("pre".equalsIgnoreCase(env)) {
            String dbName = null;
            if (bg.equalsIgnoreCase("gbg")) {
                dbName = "oneid_un_pre";
            } else if (bg.equalsIgnoreCase("gsp")) {
                dbName = "oneid_gsp_pre";
            } else if (bg.equalsIgnoreCase("cbg")) {
                dbName = "oneid_cn_pre";
            } else {
                Assert.isTrue(false, "invalid bg name");
                return null;
            }

            return DatabaseConfig.builder()
                .driverClassName("com.mysql.cj.jdbc.Driver")
                .url("jdbc:mysql://" + TEMP_PRE_HOST + ":3306/" + dbName + "?characterEncoding=UTF-8&useLocalSessionState=true&serverTimezone=GMT%2b8")
                .name("xy_read")
                .pwd("QSehxyG3X-BE")
                .build();
        } else {
            Assert.isTrue(false, "invalid bg name");
            return null;
        }
    }
}
