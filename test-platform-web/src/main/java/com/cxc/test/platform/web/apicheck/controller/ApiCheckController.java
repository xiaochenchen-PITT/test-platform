package com.cxc.test.platform.web.apicheck.controller;

import com.cxc.test.platform.common.diff.ThreadPoolFactory;
import com.cxc.test.platform.common.domain.AmisResult;
import com.cxc.test.platform.web.BaseController;
import com.cxc.test.platform.web.apicheck.vo.ApiConfigVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.ExecutorService;

@Slf4j
@CrossOrigin
@Controller
@RequestMapping(value = "/apicheck")
public class ApiCheckController extends BaseController {


    // 限制同时最多3个并发
    public final ExecutorService executorService = ThreadPoolFactory.getGeneralExecutorService();

    @GetMapping("/set")
    public String set() {
        return "apicheck/configSet";
    }

    @GetMapping("/manage")
    public String manage() {
        return "apicheck/configManage";
    }

    @GetMapping("/detail")
    public String detail() {
        return "apicheck/configDetail";
    }

    @GetMapping("/diff")
    public String diff() {
        return "apicheck/diffDetail";
    }

    @RequestMapping(value = "/add_config", method = RequestMethod.POST)
    @ResponseBody
    public AmisResult addConfig(@RequestBody ApiConfigVO apiConfigVO) {
        String triggerUrl = buildTriggerUrl();

        return null;

//        DataConfig dataConfig = convert(dataConfigVO);
//
//        Assert.isTrue(CollectionUtils.isEqualCollection(dataConfig.getTableAndInitSqlMap().keySet(), dataConfig.getRelatedSourceTables()),
//            "【源数据初始化】和【字段映射关系】中源表不一致，请检查配置");
//        Assert.isTrue(CollectionUtils.isEqualCollection(dataConfig.getTableAndLocatorMap().keySet(), dataConfig.getRelatedTargetTables()),
//            "【目标表中源数据定位】和【字段映射关系】中目标表不一致，请检查配置");
//
//        ResultDO<Long> ret = dataConfigService.addConfig(dataConfig);
//        if (ret.getIsSuccess()) {
//            return AmisResult.simpleSuccess("success", "保存成功，config id:" + ret.getData());
//        } else {
//            return AmisResult.fail(ret.getErrorMessage(), null);
//        }
    }

//    @RequestMapping(value = "/trigger", method = RequestMethod.POST)
//    @ResponseBody
//    public AmisResult trigger(@RequestParam Long configId, @RequestBody LinkedHashMap<String, Object> paramMap) {
//        try {
//            String triggerUrl = buildTriggerUrl();
//            String runningIp = String.valueOf(paramMap.get("ip"));
//            if (StringUtils.isEmpty(runningIp)) {
//                runningIp = MachineUtils.getLocalIP();
//            }
//
//            ResultDO<DataConfig> queryRet = dataConfigService.getConfig(configId);
//            if (!queryRet.getIsSuccess()) {
//                return AmisResult.fail("没有找到配置，config id: " + configId, null);
//            }
//
//            Long batchId = System.currentTimeMillis();
//
//            DataConfig dataConfig = queryRet.getData();
//            DataCheckConfig dataCheckConfig = DataCheckConfig.newInstance(true);
//
//            Map<String, Object> configMap = new HashMap<>();
//            configMap.put("dataConfig", dataConfig);
//            configMap.put("dataCheckConfig", dataCheckConfig);
//
//            String finalRunningIp = runningIp;
//            executorService.submit(() -> {
//                ResultDO<DiffResult> ret = dataCheckService.run(batchId, configId, triggerUrl, finalRunningIp, configMap);
//            });
//
//            return AmisResult.simpleSuccess("success", "触发成功");
//        } catch (Exception e) {
//            log.error("failed to trigger compare", e);
//            return AmisResult.fail(ErrorMessageUtils.getMessage(e), null);
//        }
//    }
//
//    @RequestMapping(value = "/stop", method = RequestMethod.POST)
//    @ResponseBody
//    public AmisResult stop(@RequestParam Long batchId) {
//        try {
//            String triggerUrl = buildTriggerUrl();
//
//            boolean ret = dataCheckService.stop(batchId);
//            if (ret) {
//                return AmisResult.simpleSuccess("success", "停止成功");
//            } else {
//                return AmisResult.fail("停止失败", null);
//            }
//        } catch (Exception e) {
//            log.error("failed to stop compare", e);
//            return AmisResult.fail(ErrorMessageUtils.getMessage(e), null);
//        }
//    }
//
//    @RequestMapping(value = "/get_config_list", method = RequestMethod.GET)
//    @ResponseBody
//    public AmisResult getConfigList(@RequestParam(required = false) Long configIdSearch, @RequestParam(required = false) Long batchIdSearch,
//                                    @RequestParam(required = false) String runningIpSearch, @RequestParam(required = false) String statusSearch) {
//        String triggerUrl = buildTriggerUrl();
//
//        ResultDO<Map<Long, List<DiffResultPO>>> queryRet = dataConfigService.getConfigList(configIdSearch);
//        if (!queryRet.getIsSuccess()) {
//            return AmisResult.emptySuccess();
//        }
//
//        List<DataCheckRunningItemVO> dataCheckRunningItemVOList = new ArrayList<>();
//        for (Map.Entry<Long, List<DiffResultPO>> entry : queryRet.getData().entrySet()) {
//            Long configId = entry.getKey();
//            DiffResultPO lastDiffResultPO = getLastDiffResult(entry.getValue(), batchIdSearch);
//
//            if (filterSearch(runningIpSearch, statusSearch, lastDiffResultPO)) {
//                DataCheckRunningItemVO dataCheckRunningItemVO = DataCheckRunningItemVO.builder()
//                    .batchId(lastDiffResultPO == null ? null : lastDiffResultPO.getBatchId())
//                    .configId(configId)
//                    .mappingRuleCount(parseMappingRuleCount(lastDiffResultPO))
//                    .status(lastDiffResultPO == null ? TaskStatusEnum.NOT_STARTED.getStatus() : lastDiffResultPO.getStatus())
//                    .progress(lastDiffResultPO == null ? "0" : lastDiffResultPO.getProgress().substring(0, lastDiffResultPO.getProgress().length() - 1))
//                    .runner(lastDiffResultPO == null || StringUtils.isBlank(lastDiffResultPO.getRunner()) ? "" :
//                        lastDiffResultPO.getRunner())
//                    .taskCount(parseTaskCount(lastDiffResultPO))
//                    .runningIp(lastDiffResultPO == null ? null : lastDiffResultPO.getRunningIp())
//                    .createdTime(lastDiffResultPO == null ? "" : CommonUtils.getPrettyDate(lastDiffResultPO.getCreatedTime()))
//                    .modifiedTime(lastDiffResultPO == null ? "" : CommonUtils.getPrettyDate(lastDiffResultPO.getModifiedTime()))
//                    .build();
//
//                dataCheckRunningItemVOList.add(dataCheckRunningItemVO);
//            }
//        }
//
//        JSONObject retData = new JSONObject();
//        retData.put("rows", dataCheckRunningItemVOList);
//        retData.put("count", dataCheckRunningItemVOList.size());
//
//        return AmisResult.success(retData, "ok");
//    }
//
//    @RequestMapping(value = "/delete_config", method = RequestMethod.GET)
//    @ResponseBody
//    public AmisResult deleteConfig(@RequestParam Long configId) {
//        String triggerUrl = buildTriggerUrl();
//
//        ResultDO<Boolean> ret = dataConfigService.deleteConfig(configId, false);
//        if (ret.getIsSuccess()) {
//            return AmisResult.simpleSuccess("success", "删除成功，config id:" + configId);
//        } else {
//            return AmisResult.fail(ret.getErrorMessage(), null);
//        }
//    }
//
//    @RequestMapping(value = "/get_result", method = RequestMethod.GET)
//    @ResponseBody
//    public AmisResult getResult(@RequestParam Long batchId) {
//        String triggerUrl = buildTriggerUrl();
//
//        if (batchId == null) {
//            return AmisResult.fail("batchId缺失", null);
//        }
//
//        DiffResultPO diffResultPO = diffResultMapper.getByBatchId(batchId);
//        if (diffResultPO == null) {
//            log.info("Did not find result for batch id: " + batchId);
//            return AmisResult.fail("没有找到对比结果，batchId:" + batchId, null);
//        }
//
//        DiffResultVO diffResultVO = DiffResultVO.builder()
//            .batchId(batchId)
//            .configId(diffResultPO.getConfigId())
//            .isSuccess(diffResultPO.getIsSuccess() == 1 ? "成功" : "失败")
//            .isEqual(diffResultPO.getIsEqual() == 1 ? "一致" : "不一致")
//            .status(TaskStatusEnum.getByStatus(diffResultPO.getStatus()).getLabel())
//            .progress(diffResultPO.getProgress().substring(0, diffResultPO.getProgress().length() - 1))
//            .runner(diffResultPO.getRunner())
//            .errorMessage(diffResultPO.getErrorMessage())
//            .triggerUrl(diffResultPO.getTriggerUrl())
//            .totalCount(diffResultPO.getTotalCount())
//            .failedCount(diffResultPO.getFailedCount())
//            .runningIp(diffResultPO.getRunningIp())
//            .build();
//
//        return AmisResult.success((JSONObject) JSONObject.toJSON(diffResultVO), "ok");
//    }
//
//    @RequestMapping(value = "/get_detail_list", method = RequestMethod.GET)
//    @ResponseBody
//    public AmisResult getDetailList(@RequestParam Long batchId, @RequestParam(required = false) String diffTypeSearch,
//                                    @RequestParam(required = false) String sourceSearch,
//                                    @RequestParam(required = false) String targetSearch) {
//        String triggerUrl = buildTriggerUrl();
//
//        if (batchId == null) {
//            return AmisResult.fail("batchId缺失", null);
//        }
//
//        List<DiffDetailPO> detailPOList = diffDetailMapper.getByBatchId(batchId);
//        if (CollectionUtils.isEmpty(detailPOList)) {
//            log.info("Did not find result for batch id: " + batchId);
//            return AmisResult.emptySuccess();
//        }
//
//        List<DiffDetailVO> detailVOList = new ArrayList<>();
//        for (DiffDetailPO diffDetailPO : detailPOList) {
//            if (StringUtils.isNotEmpty(diffTypeSearch) && !diffTypeSearch.equalsIgnoreCase(diffDetailPO.getDiffType())) {
//                continue;
//            }
//
//            if (StringUtils.isNotEmpty(sourceSearch) && StringUtils.isNotEmpty(diffDetailPO.getSourceQuery()) &&
//                !diffDetailPO.getSourceQuery().contains(sourceSearch)) {
//                continue;
//            }
//
//            if (StringUtils.isNotEmpty(targetSearch) && StringUtils.isNotEmpty(diffDetailPO.getTargetQuery()) &&
//                !diffDetailPO.getTargetQuery().contains(targetSearch)) {
//                continue;
//            }
//
//            DiffDetailVO diffDetailVO = DiffDetailVO.builder()
//                .id(diffDetailPO.getId())
//                .batchId(diffDetailPO.getBatchId())
//                .configId(diffDetailPO.getConfigId())
//                .diffType(diffDetailPO.getDiffType())
//                .sourceQuery(diffDetailPO.getSourceQuery())
//                .sourceTableName(diffDetailPO.getSourceTableName())
//                .sourceFieldName(diffDetailPO.getSourceFieldName())
//                .sourceValue(diffDetailPO.getSourceValue())
//                .computedSourceValue(diffDetailPO.getComputedSourceValue())
//                .targetQuery(diffDetailPO.getTargetQuery())
//                .targetTableName(diffDetailPO.getTargetTableName())
//                .targetFieldName(diffDetailPO.getTargetFieldName())
//                .targetValue(diffDetailPO.getTargetValue())
//                .errorMessage(diffDetailPO.getErrorMessage())
//                .build();
//
//            detailVOList.add(diffDetailVO);
//        }
//
//        JSONObject retData = new JSONObject();
//        retData.put("rows", detailVOList);
//        retData.put("count", detailVOList.size());
//
//        return AmisResult.success(retData, "ok");
//    }
//
//    private List<Object> parseArgFromVO(String argsStr) {
//        if (StringUtils.isBlank(argsStr)) {
//            return null;
//        }
//
//        argsStr = argsStr.replace("[", "").replace("]", "");
//
//        if (StringUtils.isBlank(argsStr)) {
//            return null;
//        }
//
//        return Arrays.stream(argsStr.split(","))
//            .map(s -> trim(s))
//            .collect(Collectors.toList());
//    }
//
//    private String convertArgsFromVO2PO(String argsStr) {
//        List<Object> argList = parseArgFromVO(argsStr);
//
//        if (CollectionUtils.isEmpty(argList)) {
//            return "";
//        }
//
//        return String.valueOf(argList);
//    }
}
