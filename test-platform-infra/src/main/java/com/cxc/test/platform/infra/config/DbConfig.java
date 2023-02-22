package com.cxc.test.platform.infra.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * todo 暂时在这写死，后续落到db中作为配置项
 */
@Slf4j
@Component
public class DbConfig {

    // 切换为上线临时公网访问链接地址
    private final String TEMP_PRE_HOST = "rm-wz9536ows19zzvnsfeo.mysql.rds.aliyuncs.com";

    public DatabaseConfig getXforceDatabaseConfig(String env, String bg) {
        if ("test".equalsIgnoreCase(env)) {
            return DatabaseConfig.builder()
                .driverClassName("com.mysql.cj.jdbc.Driver")
                .url("jdbc:mysql://mysql-0.mysql.bbmallun-middleware.svc.cluster.local:3306/bbmall_crm_test?characterEncoding=UTF-8&useLocalSessionState=true&serverTimezone=GMT%2b8")
                .name("bbmall_crm_test")
                .pwd("bbmall_crm_test")
                .build();
        } else if ("pre".equalsIgnoreCase(env)) {
            String dbName = null;
            if (bg.equalsIgnoreCase("gbg")) {
                dbName = "xforce_un_pre";
            } else if (bg.equalsIgnoreCase("gsp")) {
                dbName = "xforce_gsp_pre";
            } else {
                Assert.isTrue(false, "invalid bg name");
                return null;
            }

            return DatabaseConfig.builder()
                .driverClassName("com.mysql.cj.jdbc.Driver")
//                .url("jdbc:mysql://rm-wz9536ows19zzvnsf.mysql.rds.aliyuncs.com:3306/xforce_un_pre?characterEncoding=UTF-8&useLocalSessionState=true&serverTimezone=GMT%2b8")
//                .name("xforce")
//                .pwd("HeJXd43Hk")
                .url("jdbc:mysql://" + TEMP_PRE_HOST + ":3306/" + dbName + "?characterEncoding=UTF-8&useLocalSessionState=true&serverTimezone=GMT%2b8")
                .name("xy_read")
                .pwd("QSehxyG3X-BE")
                .build();
//        } else if ("prod".equalsIgnoreCase(env)) {
//            return DatabaseConfig.builder()
//                .driverClassName("com.mysql.cj.jdbc.Driver")
//                .url()
//                .name()
//                .pwd()
//                .build();
        } else {
            Assert.isTrue(false, "invalid env");
            return null;
        }
    }

    public DatabaseConfig getScmDatabaseConfig(String env, boolean isScmCustomer) {
        if ("test".equalsIgnoreCase(env)) {
            if (isScmCustomer) {
                return DatabaseConfig.builder()
                    .driverClassName("com.mysql.cj.jdbc.Driver")
                    .url("jdbc:mysql://mysql-0.mysql.common-middleware.svc.cluster.bberp:3306/bberp_basic_test?characterEncoding=UTF-8&useLocalSessionState=true&serverTimezone=GMT%2b8")
                    .name("root")
                    .pwd("9ZE8zUes")
                    .build();
            } else {
                return DatabaseConfig.builder()
                    .driverClassName("com.mysql.cj.jdbc.Driver")
                    .url("jdbc:mysql://mysql-0.mysql.merchant-middleware.svc.cluster.merchant:3306/merchant_test?characterEncoding=UTF-8&useLocalSessionState=true&serverTimezone=GMT%2b8")
                    .name("merchant_test")
                    .pwd("merchant_test")
                    .build();
            }
        } else if ("pre".equalsIgnoreCase(env)) {
            if (isScmCustomer) {
                return DatabaseConfig.builder()
                    .driverClassName("com.mysql.cj.jdbc.Driver")
                    .url("jdbc:mysql://" + TEMP_PRE_HOST + ":3306/bberp_basic_pre?useAffectedRows=true&rewriteBatchedStatements=true&allowMultiQueries=true&useUnicode=true&characterEncoding=UTF-8&useSSL=false&serverTimezone=GMT%2B8")
                    .name("xy_read")
                    .pwd("QSehxyG3X-BE")
                    .build();
            } else {
                return DatabaseConfig.builder()
                    .driverClassName("com.mysql.cj.jdbc.Driver")
                    .url("jdbc:mysql://" + TEMP_PRE_HOST + ":3306/merchant_advance_pre?useAffectedRows=true&rewriteBatchedStatements=true&allowMultiQueries=true&useUnicode=true&characterEncoding=UTF-8&useSSL=false&serverTimezone=GMT%2B8")
                    .name("xy_read")
                    .pwd("QSehxyG3X-BE")
                    .build();
            }
//        } else if ("prod".equalsIgnoreCase(env)) {
//            return DatabaseConfig.builder()
//                .driverClassName("com.mysql.cj.jdbc.Driver")
//                .url()
//                .name()
//                .pwd()
//                .build();
        } else {
            log.error("invalid env: " + env);
            return null;
        }
    }

    public DatabaseConfig getCrmDatabaseConfig(String env, String bg) {
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
//                .url("jdbc:mysql://rm-wz9536ows19zzvnsf.mysql.rds.aliyuncs.com:3306/ondid-cn-pre?characterEncoding=UTF-8&useLocalSessionState=true&serverTimezone=GMT%2b8")
//                .name("oneid")
//                .pwd("J3gG445FdlFG")
                .url("jdbc:mysql://" + TEMP_PRE_HOST + ":3306/" + dbName + "?characterEncoding=UTF-8&useLocalSessionState=true&serverTimezone=GMT%2b8")
                .name("xy_read")
                .pwd("QSehxyG3X-BE")
                .build();
//        } else if ("prod".equalsIgnoreCase(env)) {
//            return DatabaseConfig.builder()
//                .driverClassName("com.mysql.cj.jdbc.Driver")
//                .url()
//                .name()
//                .pwd()
//                .build();
        } else {
            Assert.isTrue(false, "invalid bg name");
            return null;
        }
    }

    public DatabaseConfig getSrmDatabaseConfig(String env, String bg) {
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
//                .url("jdbc:mysql://rm-wz9536ows19zzvnsf.mysql.rds.aliyuncs.com:3306/ondid-cn-pre?characterEncoding=UTF-8&useLocalSessionState=true&serverTimezone=GMT%2b8")
//                .name("oneid")
//                .pwd("J3gG445FdlFG")
                .url("jdbc:mysql://" + TEMP_PRE_HOST + ":3306/" + dbName + "?characterEncoding=UTF-8&useLocalSessionState=true&serverTimezone=GMT%2b8")
                .name("xy_read")
                .pwd("QSehxyG3X-BE")
                .build();
//        } else if ("prod".equalsIgnoreCase(env)) {
//            return DatabaseConfig.builder()
//                .driverClassName("com.mysql.cj.jdbc.Driver")
//                .url()
//                .name()
//                .pwd()
//                .build();
        } else {
            log.error("invalid env: " + env);
            return null;
        }
    }
}
