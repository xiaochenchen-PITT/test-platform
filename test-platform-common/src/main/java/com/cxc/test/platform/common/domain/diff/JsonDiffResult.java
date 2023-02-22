package com.cxc.test.platform.common.domain.diff;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Data
public class JsonDiffResult {

    @Getter
    @Setter
    private Boolean isEqual;

    @Setter
    private List<JsonDiffField> diffFields;

    public List<JsonDiffField> getDiffFields() {
        if (diffFields == null) {
            diffFields = new ArrayList<>();
        }

        return diffFields;
    }
}
