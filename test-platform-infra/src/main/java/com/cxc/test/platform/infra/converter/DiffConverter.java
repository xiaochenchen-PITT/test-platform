package com.cxc.test.platform.infra.converter;

import com.cxc.test.platform.common.domain.diff.DiffDetail;
import com.cxc.test.platform.common.domain.diff.DiffResult;
import com.cxc.test.platform.common.utils.CommonUtils;
import com.cxc.test.platform.infra.domain.diff.DiffDetailPO;
import com.cxc.test.platform.infra.domain.diff.DiffResultPO;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class DiffConverter {

    public DiffResultPO convertDO2PO(DiffResult diffResult) {
        DiffResultPO diffResultPO = new DiffResultPO();

        diffResultPO.setBatchId(diffResult.getBatchId());
        diffResultPO.setConfigId(diffResult.getConfigId());
        diffResultPO.setIsSuccess(BooleanUtils.isTrue(diffResult.getIsSuccess()) ? 1 : 0);
        diffResultPO.setIsEqual(BooleanUtils.isTrue(diffResult.getIsEqual()) ? 1 : 0);
        diffResultPO.setStatus(diffResult.getStatus());
        diffResultPO.setProgress(diffResult.getProgress());
        diffResultPO.setErrorMessage(diffResult.getErrorMessage());
        diffResultPO.setTriggerUrl(diffResult.getTriggerUrl());
        diffResultPO.setTotalCount(diffResult.getTotalCount());
        diffResultPO.setFailedCount(diffResult.getFailedCount());
        diffResultPO.setFeatures(String.valueOf(diffResult.getFeatures()));
        diffResultPO.setCreatedTime(diffResult.getCreatedTime());
        diffResultPO.setModifiedTime(diffResult.getModifiedTime());

        return diffResultPO;
    }

    public DiffDetailPO convertDO2PO(DiffDetail diffDetail) {
        DiffDetailPO diffDetailPO = new DiffDetailPO();

        diffDetailPO.setBatchId(diffDetail.getBatchId());
        diffDetailPO.setConfigId(diffDetail.getConfigId());
        diffDetailPO.setDiffType(diffDetail.getDiffType());
        diffDetailPO.setSourceQuery(diffDetail.getSourceQuery());
        diffDetailPO.setSourceTableName(diffDetail.getSourceTableName());
        diffDetailPO.setSourceFieldName(diffDetail.getSourceFieldName());
        diffDetailPO.setSourceValue(diffDetail.getSourceValue());
        diffDetailPO.setComputedSourceValue(diffDetail.getComputedSourceValue());
        diffDetailPO.setTargetQuery(diffDetail.getTargetQuery());
        diffDetailPO.setTargetTableName(diffDetail.getTargetTableName());
        diffDetailPO.setTargetFieldName(diffDetail.getTargetFieldName());
        diffDetailPO.setTargetValue(diffDetail.getTargetValue());
        diffDetailPO.setCreatedTime(diffDetail.getCreatedTime());
        diffDetailPO.setModifiedTime(diffDetail.getModifiedTime());
        diffDetailPO.setErrorMessage(diffDetail.getErrorMessage());

        return diffDetailPO;
    }

    public List<DiffDetailPO> convertDO2PO(List<DiffDetail> diffDetailList) {
        List<DiffDetailPO> diffDetailPOList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(diffDetailList)) {
            for (DiffDetail diffDetail : diffDetailList) {
                diffDetailPOList.add(convertDO2PO(diffDetail));
            }
        }

        return diffDetailPOList;
    }

    public DiffResult convertPO2DO(DiffResultPO diffResultPO, List<DiffDetailPO> diffDetailPOList) {
        return DiffResult.builder()
            .batchId(diffResultPO.getBatchId())
            .configId(diffResultPO.getConfigId())
            .isSuccess(diffResultPO.getIsSuccess() == 1)
            .isEqual(diffResultPO.getIsEqual() == 1)
            .status(diffResultPO.getStatus())
            .progress(diffResultPO.getProgress())
            .errorMessage(diffResultPO.getErrorMessage())
            .triggerUrl(diffResultPO.getTriggerUrl())
            .totalCount(diffResultPO.getTotalCount())
            .failedCount(diffResultPO.getFailedCount())
            .features(CommonUtils.convterToMap(diffResultPO.getFeatures()))
            .diffDetailList(convertPO2DO(diffDetailPOList))
            .createdTime(diffResultPO.getCreatedTime())
            .modifiedTime(diffResultPO.getModifiedTime())
            .build();
    }

    public DiffDetail convertPO2DO(DiffDetailPO diffDetailPO) {
        return DiffDetail.builder()
            .batchId(diffDetailPO.getBatchId())
            .configId(diffDetailPO.getConfigId())
            .diffType(diffDetailPO.getDiffType())
            .sourceQuery(diffDetailPO.getSourceQuery())
            .sourceTableName(diffDetailPO.getSourceTableName())
            .sourceFieldName(diffDetailPO.getSourceFieldName())
            .sourceValue(diffDetailPO.getSourceValue())
            .computedSourceValue(diffDetailPO.getComputedSourceValue())
            .targetQuery(diffDetailPO.getTargetQuery())
            .targetTableName(diffDetailPO.getTargetTableName())
            .targetFieldName(diffDetailPO.getTargetFieldName())
            .targetValue(diffDetailPO.getTargetValue())
            .createdTime(diffDetailPO.getCreatedTime())
            .modifiedTime(diffDetailPO.getModifiedTime())
            .errorMessage(diffDetailPO.getErrorMessage())
            .build();
    }

    public List<DiffDetail> convertPO2DO(List<DiffDetailPO> diffDetailPOList) {
        List<DiffDetail> diffDetailList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(diffDetailPOList)) {
            for (DiffDetailPO diffDetailPO : diffDetailPOList) {
                diffDetailList.add(convertPO2DO(diffDetailPO));
            }
        }

        return diffDetailList;
    }
}
