package com.increff.pos.api;

import com.increff.pos.db.ClientPojo;
import com.increff.pos.exception.ApiException;
import org.springframework.data.domain.Page;

public interface ClientApi {
    ClientPojo add(ClientPojo clientPojo) throws ApiException;

    ClientPojo getCheck(String id) throws ApiException;

    ClientPojo getCheckByClientId(String clientId) throws ApiException;

    Page<ClientPojo> getAll(int page, int size);

    ClientPojo update(String id, ClientPojo clientPojo) throws ApiException;

    java.util.Map<String, ClientPojo> getByClientIds(java.util.List<String> clientIds) throws ApiException;

    Page<ClientPojo> search(String clientId, String name, String email, int page, int size);
}
