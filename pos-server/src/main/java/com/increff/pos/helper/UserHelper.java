package com.increff.pos.helper;


import com.increff.pos.db.UserPojo;
import com.increff.pos.model.data.UserData;
import com.increff.pos.model.form.UserForm;

import java.util.List;
import java.util.stream.Collectors;

public class UserHelper {
    public static UserPojo convertToEntity(UserForm dto) {
        UserPojo userPojo = new UserPojo();
        userPojo.setName(dto.getName());
        userPojo.setEmail(dto.getEmail());
        return userPojo;
    }
    public static List<UserData> convertToUserDataList(List<UserPojo> userPojoDataList) {
        return userPojoDataList.stream().map(UserHelper::convertToDto).collect(Collectors.toList());
    }


    public static UserData convertToDto(UserPojo userPojo) {
        UserData userData = new UserData();
        userData.setId(userPojo.getId());
        userData.setName(userPojo.getName());
        userData.setEmail(userPojo.getEmail());
        return userData;
    }
}
