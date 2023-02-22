package com.cxc.test.platform.infra.converter;

import com.cxc.test.platform.common.domain.diff.DiffDetail;
import com.cxc.test.platform.common.domain.diff.DiffResult;
import com.cxc.test.platform.infra.domain.diff.DiffDetailPO;
import com.cxc.test.platform.infra.domain.diff.DiffResultPO;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class DiffConverter {

    public DiffResultPO convertDO2PO(DiffResult diffResult) {
        return DiffResultPO.builder()
            .batchId(diffResult.getBatchId())
            .isSuccess(diffResult.getIsSuccess() ? 1 : 0)
            .isEqual(diffResult.getIsEqual() ? 1 : 0)
            .errorMessage(diffResult.getErrorMessage())
            .triggerUrl(diffResult.getTriggerUrl())
            .totalCount(diffResult.getTotalCount())
            .failedCount(diffResult.getFailedCount())
            .createdTime(diffResult.getCreatedTime())
            .modifiedTime(diffResult.getModifiedTime())
            .build();
    }

    public DiffDetailPO convertDO2PO(DiffDetail diffDetail) {
        return DiffDetailPO.builder()
            .batchId(diffDetail.getBatchId())
            .diffType(diffDetail.getDiffType())
            .sourceQuerySql(diffDetail.getSourceQuerySql())
            .sourceTableName(diffDetail.getSourceTableName())
            .sourceFieldName(diffDetail.getSourceFieldName())
            .sourceValue(diffDetail.getSourceValue())
            .targetQuerySql(diffDetail.getTargetQuerySql())
            .targetTableName(diffDetail.getTargetTableName())
            .targetFieldName(diffDetail.getTargetFieldName())
            .targetValue(diffDetail.getTargetValue())
            .createdTime(diffDetail.getCreatedTime())
            .modifiedTime(diffDetail.getModifiedTime())
            .errorMessage(diffDetail.getErrorMessage())
            .build();
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
            .isSuccess(diffResultPO.getIsSuccess() == 1)
            .isEqual(diffResultPO.getIsEqual() == 1)
            .errorMessage(diffResultPO.getErrorMessage())
            .triggerUrl(diffResultPO.getTriggerUrl())
            .totalCount(diffResultPO.getTotalCount())
            .failedCount(diffResultPO.getFailedCount())
            .diffDetailList(convertPO2DO(diffDetailPOList))
            .createdTime(diffResultPO.getCreatedTime())
            .modifiedTime(diffResultPO.getModifiedTime())
            .build();
    }

    public DiffDetail convertPO2DO(DiffDetailPO diffDetailPO) {
        return DiffDetail.builder()
            .batchId(diffDetailPO.getBatchId())
            .diffType(diffDetailPO.getDiffType())
            .sourceQuerySql(diffDetailPO.getSourceQuerySql())
            .sourceTableName(diffDetailPO.getSourceTableName())
            .sourceFieldName(diffDetailPO.getSourceFieldName())
            .sourceValue(diffDetailPO.getSourceValue())
            .targetQuerySql(diffDetailPO.getTargetQuerySql())
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
