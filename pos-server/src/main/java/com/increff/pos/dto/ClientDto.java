package com.increff.pos.dto;

import com.increff.pos.api.ClientApi;
import com.increff.pos.db.ClientPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.helper.ClientHelper;
import com.increff.pos.model.data.ClientData;
import com.increff.pos.model.form.ClientForm;
import com.increff.pos.model.form.PageForm;
import com.increff.pos.util.ValidationUtil;
import com.increff.pos.util.NormalizeUtil;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
public class ClientDto {

    private final ClientApi clientApi;

    public ClientDto(ClientApi clientApi) {
        this.clientApi = clientApi;
    }

    public ClientData create(ClientForm form) throws ApiException {
        ValidationUtil.validateClientForm(form);
        NormalizeUtil.normalizeClientForm(form);
        ClientPojo pojo = ClientHelper.convertToEntity(form);
        return ClientHelper.convertToDto(clientApi.add(pojo));
    }

    public ClientData getById(String id) throws ApiException {
        return ClientHelper.convertToDto(clientApi.getCheckByClientId(id));
    }

    public Page<ClientData> getAll(PageForm form) throws ApiException {
        ValidationUtil.validatePageForm(form);
        return clientApi.getAll(form.getPage(), form.getSize()).map(ClientHelper::convertToDto);
    }

    public ClientData update(String id, ClientForm form) throws ApiException {
        ValidationUtil.validateClientForm(form);
        NormalizeUtil.normalizeClientForm(form);
        ClientPojo pojo = ClientHelper.convertToEntity(form);
        return ClientHelper.convertToDto(clientApi.update(id, pojo));
    }
}
