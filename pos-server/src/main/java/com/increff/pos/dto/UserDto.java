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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserDto {

    private final UserApi userApi;
    public UserDto(UserApi userApi) {
        this.userApi = userApi;
    }

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserData create(UserForm userForm) throws ApiException {
        ValidationUtil.validateUserForm(userForm);
        
        // Prevent creating supervisor users via API - supervisors are auto-created from properties
        if (userForm.getRole() != null && "SUPERVISOR".equals(userForm.getRole())) {
            throw new ApiException("Supervisor users cannot be created via API. They are automatically created from application properties.");
        }
        
        UserPojo userPojo = UserHelper.convertToEntity(userForm);
        
        // Operators don't need passwords - they login with email only
        // No password hashing needed for operators
        
        // Force role to be USER (OPERATOR) - supervisors can only be created via initialization
        userPojo.setRole("USER");
        
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