package com.increff.pos.api;

import com.increff.pos.dao.UserDao;
import com.increff.pos.db.UserPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.model.form.PageForm;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
public class UserApiImpl implements UserApi {

    private final UserDao dao;

    public UserApiImpl(UserDao dao) {
        this.dao = dao;
    }

    @Override
    @Transactional(rollbackFor = ApiException.class)
    public UserPojo add(UserPojo userPojo) throws ApiException {
        checkIfEmailExists(userPojo.getEmail());
        return dao.save(userPojo);
    }

    @Override
    @Transactional(readOnly = true)
    public UserPojo getCheck(String id) throws ApiException {
        return dao.findById(id).orElseThrow(() -> new ApiException("User not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserPojo> getAll() {
        return dao.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public UserPojo getByEmail(String email) {
        return dao.findByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserPojo> getAll(PageForm form) {
        PageRequest pageRequest = PageRequest.of(form.getPage(), form.getSize(),
                Sort.by(Sort.Direction.DESC, "createdAt"));
        return dao.findAll(pageRequest);
    }

    private void checkIfEmailExists(String email) throws ApiException {
        UserPojo existingUserPojo = dao.findByEmail(email);
        if (Objects.nonNull(existingUserPojo)) {
            throw new ApiException("User already exists with email: " + email);
        }
    }
}