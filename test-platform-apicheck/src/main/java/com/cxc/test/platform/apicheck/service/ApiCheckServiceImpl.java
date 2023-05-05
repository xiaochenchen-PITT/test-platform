package com.cxc.test.platform.apicheck.service;

import com.cxc.test.platform.common.diff.GeneralDiffCheckFacade;
import com.cxc.test.platform.common.domain.ResultDO;
import com.cxc.test.platform.common.domain.diff.DiffResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
public class ApiCheckServiceImpl implements GeneralDiffCheckFacade {

    @Override
    public DiffResult init(Long batchId, Long configId, String triggerUrl, String runningIp) {
        return null;
    }

    @Override
    public DiffResult end(DiffResult diffResult) {
        return null;
    }

    @Override
    public boolean isRunning(Long batchId) {
        return false;
    }

    @Override
    public ResultDO<DiffResult> run(Long batchId, Long configId, String triggerUrl, String runningIp, Map<String, Object> configMap) {
        return null;
    }

    @Override
    public boolean stop(Long batchId) {
        return false;
    }

    @Override
    public void updateDiffResultOnTime() {

    }
}
