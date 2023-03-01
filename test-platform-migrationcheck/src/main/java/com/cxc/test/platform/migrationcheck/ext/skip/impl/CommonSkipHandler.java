package com.cxc.test.platform.migrationcheck.ext.skip.impl;

import com.cxc.test.platform.migrationcheck.domain.config.MigrationCheckConfig;
import com.cxc.test.platform.migrationcheck.domain.data.MigrationData;
import com.cxc.test.platform.migrationcheck.ext.skip.SkipCheckHandler;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class CommonSkipHandler implements SkipCheckHandler {

    // 不好处理的字段，检查之后跳过
    private static List<String> SKIP_FIELDS = new ArrayList<>();

    static {
        SKIP_FIELDS.add("t_merchants_cooperative_information#fjoint_operation_status"); // 已知问题
        SKIP_FIELDS.add("t_merchants_supplier_basic_info#fsupplier_type"); // 2和true的问题
        SKIP_FIELDS.add("t_bb_company_info#Ftax_regist_num"); // 自定义逻辑入参是动态获取的，无法支持
        SKIP_FIELDS.add("t_bb_company_voucher#Fis_delete"); // 已知问题
        SKIP_FIELDS.add("t_bb_company_role#Finfo_auditor_aid_list"); // 待定位，但开发反馈没问题，先跳过吧
    }

    @Override
    public String getName() {
        return "commonSkipHandler";
    }

    @Override
    public boolean shouldSkip(String sourceTableName, String sourceFieldName, MigrationData sourceData, MigrationCheckConfig migrationCheckConfig) {
        if (StringUtils.isEmpty(sourceTableName) || StringUtils.isEmpty(sourceFieldName)) {
            return false;
        }

        if (SKIP_FIELDS.contains(sourceTableName + "#" + sourceFieldName)) {
            return true;
        }

        return false;
    }
}
