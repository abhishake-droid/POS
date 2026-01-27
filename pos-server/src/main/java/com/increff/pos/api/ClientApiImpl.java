package com.increff.pos.api;

import com.increff.pos.dao.ClientDao;
import com.increff.pos.db.ClientPojo;
import com.increff.pos.exception.ApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.increff.pos.util.SequenceGenerator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Objects;

@Service
public class ClientApiImpl implements ClientApi {
    private static final Logger logger = LoggerFactory.getLogger(ClientApiImpl.class);

    private final ClientDao dao;
    private final SequenceGenerator sequenceGenerator;


    public ClientApiImpl(ClientDao dao, SequenceGenerator sequenceGenerator) {
        this.dao = dao;
        this.sequenceGenerator = sequenceGenerator;
    }


    @Override
    @Transactional(rollbackFor = ApiException.class)
    public ClientPojo add(ClientPojo clientPojo) throws ApiException {
        logger.info("Creating client name: {}", clientPojo.getName());

        // Validation
        // Name validation
        validateNameFormat(clientPojo.getName());
        String normalizedName = clientPojo.getName().trim().toLowerCase();
        if (dao.findByName(normalizedName) != null) {
            throw new ApiException("Client name already exists");
        }

        // Phone validation
        validatePhoneFormat(clientPojo.getPhone());
        if (dao.findByPhone(clientPojo.getPhone()) != null) {
            throw new ApiException("Phone number already exists");
        }

        // Email validation
        validateEmailFormat(clientPojo.getEmail());
        if (dao.findByEmail(clientPojo.getEmail()) != null) {
            throw new ApiException("Email already exists");
        }

        String name = clientPojo.getName().trim().toLowerCase();

        long sequence = sequenceGenerator.getNextSequence("client");
        String clientId = String.format("C%04d", sequence);
        clientPojo.setClientId(clientId);

        clientPojo.setName(name);

        clientPojo.setPhone(clientPojo.getPhone().trim());
        clientPojo.setEmail(clientPojo.getEmail().trim().toLowerCase());

        ClientPojo saved = dao.save(clientPojo);

        logger.info("Client created successfully with clientId: {}", saved.getClientId());

        return saved;
    }

    @Override
    public ClientPojo get(String id) throws ApiException {
        ClientPojo clientPojo = dao.findById(id).orElse(null);
        if (Objects.isNull(clientPojo)) {
            throw new ApiException("Client not found with id: " + id);
        }
        return clientPojo;
    }

    @Override
    public ClientPojo getById(String clientId) throws ApiException {
        ClientPojo clientPojo = dao.findByClientId(
                clientId != null ? clientId.trim() : null
        );

        if (Objects.isNull(clientPojo)) {
            throw new ApiException("Client not found with clientId: " + clientId);
        }
        return clientPojo;
    }


    @Override
    public Page<ClientPojo> getAll(int page, int size) {
        logger.info("Fetching clients page {} with size {}", page, size);
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return dao.findAll(pageRequest);
    }

    @Override
    @Transactional(rollbackFor = ApiException.class)
    public ClientPojo update(String id, ClientPojo clientPojo) throws ApiException {
        logger.info("Updating client with id: {}", id);

        ClientPojo existing = get(id);

        // Validate name
        if (clientPojo.getName() == null || clientPojo.getName().trim().isEmpty()) {
            throw new ApiException("Client name cannot be empty");
        }

        // PHONE VALIDATION
        if (clientPojo.getPhone() != null) {
            String phone = clientPojo.getPhone().trim();

            validatePhoneFormat(phone);

            ClientPojo phoneDuplicate = dao.findByPhone(phone);
            if (phoneDuplicate != null && !phoneDuplicate.getId().equals(id)) {
                throw new ApiException("Phone number already exists");
            }

            existing.setPhone(phone);
        }

        // EMAIL VALIDATION
        if (clientPojo.getEmail() != null) {
            String email = clientPojo.getEmail().trim().toLowerCase();

            validateEmailFormat(email);

            ClientPojo emailDuplicate = dao.findByEmail(email);
            if (emailDuplicate != null && !emailDuplicate.getId().equals(id)) {
                throw new ApiException("Email already exists");
            }

            existing.setEmail(email);
        }

        // Update name
        existing.setName(clientPojo.getName().trim().toLowerCase());

        ClientPojo updated = dao.save(existing);
        logger.info("Updated client with id: {}", updated.getId());
        return updated;
    }

    private void validatePhoneFormat(String phone) throws ApiException {
        if (phone == null || phone.trim().isEmpty()) {
            throw new ApiException("Phone number cannot be empty");
        }

        if (!phone.trim().matches("\\d{10}")) {
            throw new ApiException("Phone number must be exactly 10 digits");
        }
    }


    private void validateEmailFormat(String email) throws ApiException {
        if (email == null || email.trim().isEmpty()) {
            throw new ApiException("Email cannot be empty");
        }

        if (!email.trim().matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new ApiException("Invalid email format");
        }
    }

    private void validateNameFormat(String name) throws ApiException {
        if (name == null || name.trim().isEmpty()) {
            throw new ApiException("Client name cannot be empty");
        }
        if (name.length() < 3 || name.length() > 21) {
            throw new ApiException("Client name must be between 3 and 21 characters");
        }
    }



}
