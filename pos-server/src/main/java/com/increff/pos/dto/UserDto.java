package com.increff.pos.dto;

import com.increff.pos.api.UserApi;
import com.increff.pos.helper.UserHelper;
import com.increff.pos.model.data.UserData;
import com.increff.pos.model.form.UserForm;
import com.increff.pos.db.UserPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.model.form.PageForm;
import com.increff.pos.util.ValidationUtil;
import com.increff.pos.util.NormalizeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserDto {

    @Autowired
    private UserApi userApi;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserData create(UserForm userForm) throws ApiException {
        NormalizeUtil.normalizeUserForm(userForm);
        ValidationUtil.validate(userForm);

        if (userForm.getRole() != null && "SUPERVISOR".equals(userForm.getRole())) {
            throw new ApiException(
                    "Supervisor users cannot be created via API. They are automatically created from properties.");
        }

        UserPojo userPojo = UserHelper.convertToEntity(userForm);

        if (userForm.getPassword() != null && !userForm.getPassword().isEmpty()) {
            userPojo.setPassword(passwordEncoder.encode(userForm.getPassword()));
        }

        userPojo.setRole("USER");

        UserPojo savedUserPojo = userApi.add(userPojo);
        return UserHelper.convertToData(savedUserPojo);
    }

    public UserData getById(String id) throws ApiException {
        UserPojo userPojo = userApi.getCheck(id);
        return UserHelper.convertToData(userPojo);
    }

    public Page<UserData> getAll(PageForm form) throws ApiException {
        ValidationUtil.validate(form);
        Page<UserPojo> userPage = userApi.getAll(form);
        return userPage.map(UserHelper::convertToData);
    }
}