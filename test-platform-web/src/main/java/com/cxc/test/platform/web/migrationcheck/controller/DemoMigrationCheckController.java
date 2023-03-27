package com.cxc.test.platform.web.migrationcheck.controller;

import com.cxc.test.platform.common.domain.AmisResult;
import com.cxc.test.platform.common.domain.ResultDO;
import com.cxc.test.platform.common.domain.diff.DiffResult;
import com.cxc.test.platform.infra.config.DbConfig;
import com.cxc.test.platform.migrationcheck.domain.CustomizedMethod;
import com.cxc.test.platform.migrationcheck.domain.config.MigrationCheckConfig;
import com.cxc.test.platform.migrationcheck.domain.config.MigrationConfig;
import com.cxc.test.platform.migrationcheck.domain.SourceLocator;
import com.cxc.test.platform.migrationcheck.domain.mapping.MappingRule;
import com.cxc.test.platform.migrationcheck.service.MigrationCheckService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 作为一个例子参考
 */
@Slf4j
@CrossOrigin
@RestController
@RequestMapping(value = "/demoMigrationCheck")
public class DemoMigrationCheckController extends MigrationCheckController {

    @Resource
    DbConfig dbConfig;

    @Resource
    MigrationCheckService migrationCheckService;

    public final ExecutorService singleExecutorService = Executors.newFixedThreadPool(1);

    // config的格式：表1@字段1,字段2,...,字段n;表2@字段1,字段2,...,字段n
    // /testCbgCrm?env=test&config=table1@f1,f2;table2@f3,f4
    @RequestMapping(value = "/testDemo", method = RequestMethod.GET)
    public AmisResult testDemo(@RequestParam(value = "env") String env, @RequestParam(value = "config", required = false) String config,
                               @RequestParam(value = "limit", required = false) Integer limit) {
        String triggerUrl = buildTriggerUrl();

        // init migration check config
        MigrationCheckConfig migrationCheckConfig = parseConfig(config);

        // init mapping config
        MigrationConfig migrationConfig = new MigrationConfig();

        migrationConfig.setSourceDbConfig(dbConfig.getDemoDatabaseConfig(env, "gbg"));
        migrationConfig.setTargetDbConfig(dbConfig.getDemoDatabaseConfig(env, "cbg"));

        // 录入mapping rule字段对应关系
        List<MappingRule> mappingRuleList = parseFromExcel("demo_mapping_excel.xlsx");
        migrationConfig.setMappingRuleList(mappingRuleList);

        // 源数据初始化
        String sqlLimit = buildSqlLimitClause(limit);

        Map<String, String> tableAndInitSqlMap = new HashMap<>();
        tableAndInitSqlMap.put("t_bb_company", String.format("select * from t_bb_company %s", sqlLimit));
        tableAndInitSqlMap.put("t_bb_company_balance_account", String.format("select * from t_bb_company_balance_account %s", sqlLimit));
        tableAndInitSqlMap.put("t_bb_company_info", String.format("select * from t_bb_company_info %s", sqlLimit));
        tableAndInitSqlMap.put("t_bb_company_link", String.format("select * from t_bb_company_link %s", sqlLimit));
        tableAndInitSqlMap.put("t_bb_company_role", String.format("select * from t_bb_company_role %s", sqlLimit));
        tableAndInitSqlMap.put("t_bb_company_voucher", String.format("select * from t_bb_company_voucher %s", sqlLimit));
        migrationConfig.setTableAndInitSqlMap(tableAndInitSqlMap);

        // 目标表中原表主键id定位
        SourceLocator locator = SourceLocator.builder()
            .locateField("biz_features")
            .locateMethod(CustomizedMethod.builder()
                .beanName("jsonExtractSourceId")
                .build())
            .build();
        Map<String, SourceLocator> tableAndLocatorMap = new HashMap<>();
        for (MappingRule mappingRule : migrationConfig.getValidMappingRuleList()){
            // 所有目标表都用同一个字段（biz_features）承载原表主键id，同时定位方法也都一样
            String targetTable = mappingRule.getTargetMappingItem().getTableName();
            String targetField = mappingRule.getTargetMappingItem().getFieldName();

            tableAndLocatorMap.put(targetTable, locator);
        }
        migrationConfig.setTableAndLocatorMap(tableAndLocatorMap);

        Long batchId = System.currentTimeMillis();
        singleExecutorService.submit(() -> {
            ResultDO<DiffResult> ret = migrationCheckService.compare(batchId, 0L, migrationConfig, migrationCheckConfig,
                triggerUrl, "127.0.0.1:8080");
//        System.out.println(ret);
        });

        String retInfo = String.format("starting migratrion check, env: %s, limit: %s, please check later with batch id: %s", env, limit, batchId);
        return AmisResult.simpleSuccess(retInfo, null);
    }
}
