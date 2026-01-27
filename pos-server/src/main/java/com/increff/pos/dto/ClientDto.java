package com.increff.pos.dto;

import com.increff.pos.api.ClientApi;
import com.increff.pos.db.ClientPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.helper.ClientHelper;
import com.increff.pos.model.data.ClientData;
import com.increff.pos.model.form.ClientForm;
import com.increff.pos.model.form.PageForm;
import com.increff.pos.util.ValidationUtil;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
public class ClientDto {

    private final ClientApi clientApi;

    public ClientDto(ClientApi clientApi) {
        this.clientApi = clientApi;
    }

    public ClientData create(ClientForm form) throws ApiException {
        validateClientForm(form);
        ClientPojo pojo = ClientHelper.convertToEntity(form);
        ClientPojo saved = clientApi.add(pojo);
        return ClientHelper.convertToDto(saved);
    }

    public ClientData getById(String id) throws ApiException {
        ClientPojo pojo = clientApi.getById(id);
        return ClientHelper.convertToDto(pojo);
    }

    public Page<ClientData> getAll(PageForm form) throws ApiException {
        ValidationUtil.validatePageForm(form);
        Page<ClientPojo> pojoPage = clientApi.getAll(form.getPage(), form.getSize());
        return pojoPage.map(ClientHelper::convertToDto);
    }

    public ClientData update(String id, ClientForm form) throws ApiException {
        validateClientForm(form);
        ClientPojo pojo = ClientHelper.convertToEntity(form);
        ClientPojo updated = clientApi.update(id, pojo);
        return ClientHelper.convertToDto(updated);
    }

    private void validateClientForm(ClientForm form) throws ApiException {

        if (form.getName() == null || form.getName().trim().isEmpty()) {
            throw new ApiException("Client name cannot be empty");
        }

        if (form.getName().length() < 3 || form.getName().length() > 21) {
            throw new ApiException("Name must be between 3 and 21 characters");
        }

        if (form.getPhone() == null || !form.getPhone().matches("\\d{10}")) {
            throw new ApiException("Phone number must be 10 digits");
        }

        if (form.getEmail() == null ||
                !form.getEmail().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            throw new ApiException("Invalid email address");
        }
    }

}
