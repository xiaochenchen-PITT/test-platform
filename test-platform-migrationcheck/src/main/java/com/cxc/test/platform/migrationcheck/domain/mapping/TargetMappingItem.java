package com.cxc.test.platform.migrationcheck.domain.mapping;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TargetMappingItem {

    private String tableName;

    private String fieldName;
}
