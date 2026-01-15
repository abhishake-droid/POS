package com.increff.pos.dto;

import com.increff.pos.api.UserApi;
import com.increff.pos.helper.UserHelper;
import com.increff.pos.model.data.UserData;
import com.increff.pos.model.form.UserForm;
import com.increff.pos.db.UserPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.model.form.PageForm;
import com.increff.pos.util.ValidationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserDto {
    @Autowired
    private UserApi userApi;

    public UserData create(UserForm userForm) throws ApiException {
        ValidationUtil.validateUserForm(userForm);
        UserPojo userPojo = UserHelper.convertToEntity(userForm);
        UserPojo savedUserPojo = userApi.add(userPojo);
        return UserHelper.convertToDto(savedUserPojo);
    }

    public UserData getById(String id) throws ApiException {
        UserPojo userPojo = userApi.get(id);
        return UserHelper.convertToDto(userPojo);
    }

    public Page<UserData> getAll(PageForm form) throws ApiException {
        ValidationUtil.validatePageForm(form);
        Page<UserPojo> userPage = userApi.getAll(form.getPage(), form.getSize());
        return userPage.map(UserHelper::convertToDto);
    }
} 