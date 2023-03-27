package com.cxc.test.platform.datacheck.ext.skip.impl.business.oneid;

import com.cxc.test.platform.infra.config.DbConfig;
import com.cxc.test.platform.infra.utils.JdbcUtils;
import com.cxc.test.platform.datacheck.domain.config.DataCheckConfig;
import com.cxc.test.platform.datacheck.domain.SourceData;
import com.cxc.test.platform.datacheck.ext.skip.SkipCheckHandler;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * fsupplier_type = 3代表是物流服务商，不迁移
 */
@Component
public class FsupplierTypeSkipHandler implements SkipCheckHandler {

    @Resource
    DbConfig dbConfig;

    @Override
    public String getName() {
        return "fsupplierTypeSkipHandler";
    }

    @Override
    public boolean shouldSkip(String sourceTableName, String sourceFieldName, SourceData sourceData, DataCheckConfig dataCheckConfig) {
        if (StringUtils.isEmpty(sourceTableName) || sourceData == null) {
            return false;
        }

        Map<String, String> tableAndFKeyMap = buildMap();
        if (!tableAndFKeyMap.containsKey(sourceTableName)) {
            return false;
        }

        String fsupplierType = getFsupplierType(String.valueOf(sourceData.getValue(tableAndFKeyMap.get(sourceTableName))));
        // fsupplier_type = 3代表是物流服务商，不迁移
        if (StringUtils.equals(fsupplierType, "3")) {
            return true;
        }

        return false;
    }

    private Map<String, String> buildMap() {
        Map<String, String> tableAndFKeyMap = new HashMap<>();
        tableAndFKeyMap.put("t_merchants_bank_invoice", "fbasic_id");
        tableAndFKeyMap.put("t_merchants_company_link", "fcompany_id");
        tableAndFKeyMap.put("t_merchants_cooperative_information", "fbasic_id");
        tableAndFKeyMap.put("t_merchants_enterprise_domestic_info", "fbasic_id");
        tableAndFKeyMap.put("t_merchants_enterprise_overseas_info", "fbasic_id");
        tableAndFKeyMap.put("t_merchants_product_info", "fbasic_id");
        tableAndFKeyMap.put("t_merchants_product_qualification", "fbasic_id");
        tableAndFKeyMap.put("t_merchants_supplier_basic_info", "fid");
        tableAndFKeyMap.put("t_merchants_supplier_subject_info", "fbasic_id");

        return tableAndFKeyMap;
    }

    private String getFsupplierType(String fid) {
        String sql = "select fsupplier_type from t_merchants_supplier_basic_info where fid = " + fid;
        DataSource ds = JdbcUtils.intiDataSource(dbConfig.getDemoDatabaseConfig("test", "cbg"));
        return String.valueOf(JdbcUtils.getSingleValueBySql(ds, sql, "fsupplier_type"));
    }
}
