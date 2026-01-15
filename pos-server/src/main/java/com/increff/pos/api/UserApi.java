package com.increff.pos.api;

import com.increff.pos.db.UserPojo;
import com.increff.pos.exception.ApiException;
import org.springframework.data.domain.Page;

import java.util.List;

public interface UserApi {
    UserPojo add(UserPojo userPojo) throws ApiException;
    UserPojo get(String id) throws ApiException;
    List<UserPojo> getAll();
    UserPojo getByEmail(String email);
    Page<UserPojo> getAll(int page, int size);
} 