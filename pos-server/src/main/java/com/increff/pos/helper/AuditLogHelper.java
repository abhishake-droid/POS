package com.increff.pos.helper;

import com.increff.pos.db.AuditLogPojo;
import com.increff.pos.model.data.AuditLogData;

import java.util.List;
import java.util.stream.Collectors;

public class AuditLogHelper {
    public static AuditLogData convertToData(AuditLogPojo pojo) {
        AuditLogData data = new AuditLogData();
        data.setId(pojo.getId());
        data.setOperatorEmail(pojo.getOperatorEmail());
        data.setOperatorName(pojo.getOperatorName());
        data.setAction(pojo.getAction());
        data.setTimestamp(pojo.getTimestamp());
        return data;
    }

    public static List<AuditLogData> convertToDataList(List<AuditLogPojo> pojoList) {
        return pojoList.stream().map(AuditLogHelper::convertToData).collect(Collectors.toList());
    }
}
