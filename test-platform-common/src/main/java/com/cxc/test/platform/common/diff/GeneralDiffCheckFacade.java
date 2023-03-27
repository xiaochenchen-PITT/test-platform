package com.cxc.test.platform.common.diff;

import com.cxc.test.platform.common.domain.ResultDO;
import com.cxc.test.platform.common.domain.diff.DiffResult;

import java.util.Map;

public interface GeneralDiffCheckFacade {

    /**
     * 初始化该校验批次
     *
     * @param batchId 批次id
     * @param configId 配置id
     * @param triggerUrl 触发的url
     * @param runningIp 运行ip
     * @return
     */
    DiffResult init(Long batchId, Long configId, String triggerUrl, String runningIp);

    /**
     * 任务完成/失败后，停止该校验批次
     *
     * @param diffResult 校验结果
     * @return
     */
    DiffResult end(DiffResult diffResult);

    /**
     * 该批次是否在运行
     * @return
     */
    boolean isRunning();

    /**
     * 触发执行校验
     *
     * @param batchId 批次id
     * @param configId 配置id
     * @param triggerUrl 触发的url
     * @param runningIp 运行ip
     * @param configMap 额外需要的配置map
     * @return
     */
    ResultDO<DiffResult> run(Long batchId, Long configId, String triggerUrl, String runningIp, Map<String, Object> configMap);

    /**
     * 手动停止该批次校验
     * @return
     */
    boolean stop();

    /**
     * 定时更新校验结果
     * 需要实现定时调用
     */
    void updateDiffResultOnTime();
}
