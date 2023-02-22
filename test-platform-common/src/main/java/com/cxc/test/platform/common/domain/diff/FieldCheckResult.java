package com.cxc.test.platform.common.domain.diff;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FieldCheckResult {

    /**
     * 对比是否通过，例如前后一致，逻辑处理之后相等，或者feature/json类的包含关系等
     */
    private boolean isPass;

    /**
     * 若有自定义处理逻辑，则为自定义逻辑处理之后的值；若无，则为sourceValue不变
     */
    private Object computedValue;

}
