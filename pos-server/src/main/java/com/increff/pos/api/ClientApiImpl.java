package com.increff.pos.api;

import com.increff.pos.dao.ClientDao;
import com.increff.pos.db.ClientPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.util.SequenceGenerator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Objects;

@Service
public class ClientApiImpl implements ClientApi {

    private final ClientDao dao;
    private final SequenceGenerator sequenceGenerator;

    public ClientApiImpl(ClientDao dao, SequenceGenerator sequenceGenerator) {
        this.dao = dao;
        this.sequenceGenerator = sequenceGenerator;
    }

    @Override
    @Transactional(rollbackFor = ApiException.class)
    public ClientPojo add(ClientPojo clientPojo) throws ApiException {
        if (dao.findByName(clientPojo.getName()) != null) {
            throw new ApiException("Client name already exists");
        }
        if (dao.findByPhone(clientPojo.getPhone()) != null) {
            throw new ApiException("Phone number already exists");
        }
        if (dao.findByEmail(clientPojo.getEmail()) != null) {
            throw new ApiException("Email already exists");
        }

        long sequence = sequenceGenerator.getNextSequence("client");
        clientPojo.setClientId(String.format("C%04d", sequence));
        return dao.save(clientPojo);
    }

    @Override
    public ClientPojo getCheck(String id) throws ApiException {
        ClientPojo clientPojo = dao.findById(id).orElse(null);
        if (Objects.isNull(clientPojo)) {
            throw new ApiException("Client not found with id: " + id);
        }
        return clientPojo;
    }

    @Override
    public ClientPojo getCheckByClientId(String clientId) throws ApiException {
        ClientPojo clientPojo = dao.findByClientId(clientId);
        if (Objects.isNull(clientPojo)) {
            throw new ApiException("Client not found with clientId: " + clientId);
        }
        return clientPojo;
    }

    @Override
    public Page<ClientPojo> getAll(int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return dao.findAll(pageRequest);
    }

    @Override
    @Transactional(rollbackFor = ApiException.class)
    public ClientPojo update(String id, ClientPojo clientPojo) throws ApiException {
        ClientPojo existing = getCheck(id);

        if (dao.findByName(clientPojo.getName()) != null && !dao.findByName(clientPojo.getName()).getId().equals(id)) {
            throw new ApiException("Client name already exists");
        }

        ClientPojo phoneDuplicate = dao.findByPhone(clientPojo.getPhone());
        if (phoneDuplicate != null && !phoneDuplicate.getId().equals(id)) {
            throw new ApiException("Phone number already exists");
        }

        ClientPojo emailDuplicate = dao.findByEmail(clientPojo.getEmail());
        if (emailDuplicate != null && !emailDuplicate.getId().equals(id)) {
            throw new ApiException("Email already exists");
        }

        existing.setName(clientPojo.getName());
        existing.setPhone(clientPojo.getPhone());
        existing.setEmail(clientPojo.getEmail());

        return dao.save(existing);
    }
}
