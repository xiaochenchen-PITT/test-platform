package com.cxc.test.platform.migration.domain.mapping;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SourceMappingItem {

    private String tableName;

    private String fieldName;

    private boolean isPrimaryKey = false;
}
