package com.increff.pos.api;

import com.increff.pos.db.UserPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.model.form.PageForm;
import org.springframework.data.domain.Page;

import java.util.List;

public interface UserApi {
    UserPojo add(UserPojo userPojo) throws ApiException;

    UserPojo getCheck(String id) throws ApiException;

    List<UserPojo> getAll();

    UserPojo getByEmail(String email);

    Page<UserPojo> getAll(PageForm form);
}