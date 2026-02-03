package com.increff.pos.helper;

import com.increff.pos.db.UserPojo;
import com.increff.pos.model.data.UserData;
import com.increff.pos.model.form.UserForm;

public class UserHelper {

    public static UserPojo convertToEntity(UserForm dto) {
        UserPojo userPojo = new UserPojo();
        userPojo.setName(dto.getName());
        userPojo.setEmail(dto.getEmail());
        userPojo.setRole(dto.getRole() != null ? dto.getRole() : "USER");
        userPojo.setPassword(dto.getPassword());
        return userPojo;
    }

    public static UserData convertToData(UserPojo userPojo) {
        UserData userData = new UserData();
        userData.setId(userPojo.getId());
        userData.setName(userPojo.getName());
        userData.setEmail(userPojo.getEmail());
        userData.setRole(userPojo.getRole() != null ? userPojo.getRole() : "USER");
        return userData;
    }
}
