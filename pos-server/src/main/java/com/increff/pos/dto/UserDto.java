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
import org.springframework.data.domain.Page;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserDto {

    private final UserApi userApi;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserDto(UserApi userApi) {
        this.userApi = userApi;
    }

    public UserData create(UserForm userForm) throws ApiException {
        ValidationUtil.validateUserForm(userForm);
        NormalizeUtil.normalizeUserForm(userForm);

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
        return UserHelper.convertToDto(savedUserPojo);
    }

    public UserData getById(String id) throws ApiException {
        UserPojo userPojo = userApi.getCheck(id);
        return UserHelper.convertToDto(userPojo);
    }

    public Page<UserData> getAll(PageForm form) throws ApiException {
        ValidationUtil.validatePageForm(form);
        Page<UserPojo> userPage = userApi.getAll(form);
        return userPage.map(UserHelper::convertToDto);
    }
}