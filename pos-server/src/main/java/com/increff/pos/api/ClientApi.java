package com.increff.pos.api;

import com.increff.pos.db.ClientPojo;
import com.increff.pos.exception.ApiException;
import org.springframework.data.domain.Page;

public interface ClientApi {
    ClientPojo add(ClientPojo clientPojo) throws ApiException;
    ClientPojo get(String id) throws ApiException;
    ClientPojo getById(String clientId) throws ApiException;
    Page<ClientPojo> getAll(int page, int size);
    ClientPojo update(String id, ClientPojo clientPojo) throws ApiException;
}
