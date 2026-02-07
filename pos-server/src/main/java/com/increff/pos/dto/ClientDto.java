package com.increff.pos.dto;

import com.increff.pos.api.ClientApi;
import com.increff.pos.db.ClientPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.helper.ClientHelper;
import com.increff.pos.model.data.ClientData;
import com.increff.pos.model.form.ClientForm;
import com.increff.pos.model.form.ClientSearchForm;
import com.increff.pos.model.form.PageForm;
import com.increff.pos.util.ValidationUtil;
import com.increff.pos.util.NormalizeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
public class ClientDto {

    @Autowired
    private ClientApi clientApi;

    public ClientData create(ClientForm form) throws ApiException {
        NormalizeUtil.normalizeClientForm(form);
        ValidationUtil.validate(form);
        ClientPojo pojo = ClientHelper.convertToEntity(form);
        return ClientHelper.convertToData(clientApi.add(pojo));
    }

    public ClientData getById(String id) throws ApiException {
        id = NormalizeUtil.normalizeId(id);
        return ClientHelper.convertToData(clientApi.getCheckByClientId(id));
    }

    public Page<ClientData> getAll(PageForm form) throws ApiException {
        ValidationUtil.validate(form);
        return clientApi.getAll(form.getPage(), form.getSize()).map(ClientHelper::convertToData);
    }

    public Page<ClientData> search(ClientSearchForm form) throws ApiException {
        ValidationUtil.validate(form);
        int page = form.getPage() != null ? form.getPage() : 0;
        int size = form.getSize() != null ? form.getSize() : 10;

        String clientId = NormalizeUtil.normalizeSearchString(form.getClientId());
        String name = NormalizeUtil.normalizeSearchString(form.getName());
        String email = NormalizeUtil.normalizeSearchString(form.getEmail());

        Page<ClientPojo> clientPage = clientApi.search(clientId, name, email, page, size);
        return clientPage.map(ClientHelper::convertToData);
    }

    public ClientData update(String id, ClientForm form) throws ApiException {
        NormalizeUtil.normalizeClientForm(form);
        ValidationUtil.validate(form);
        id = NormalizeUtil.normalizeId(id);
        ClientPojo pojo = ClientHelper.convertToEntity(form);
        return ClientHelper.convertToData(clientApi.update(id, pojo));
    }
}
