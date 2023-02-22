package com.cxc.test.platform.migration.domain.data;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class MigrationFullData {

    private List<MigrationData> fullData;

    public void add(MigrationData data) {
        if (fullData == null) {
            fullData = new ArrayList<>();
        }

        fullData.add(data);
        return;
    }

}
