package com.increff.pos.helper;

import com.increff.pos.db.ClientPojo;
import com.increff.pos.model.data.ClientData;
import com.increff.pos.model.form.ClientForm;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Helper class for Client entity conversions
 */
public class ClientHelper {

    public static ClientPojo convertToEntity(ClientForm form) {
        ClientPojo pojo = new ClientPojo();
        pojo.setName(form.getName().trim().toLowerCase());
        pojo.setEmail(form.getEmail().trim().toLowerCase());
        pojo.setPhone(form.getPhone().trim());
        return pojo;
    }

    public static ClientData convertToDto(ClientPojo pojo) {
        ClientData data = new ClientData();
        data.setId(pojo.getId());
        data.setClientId(pojo.getClientId());
        data.setName(pojo.getName());
        data.setEmail(pojo.getEmail());
        data.setPhone(pojo.getPhone());
        return data;
    }


    public static List<ClientData> convertToDataList(List<ClientPojo> pojoList) {
        return pojoList.stream().map(ClientHelper::convertToDto).collect(Collectors.toList());
    }
}
