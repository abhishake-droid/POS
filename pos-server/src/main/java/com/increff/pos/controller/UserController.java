package com.increff.pos.controller;

import com.increff.pos.model.data.UserData;
import com.increff.pos.model.form.UserForm;
import com.increff.pos.model.form.PageForm;
import com.increff.pos.dto.UserDto;
import com.increff.pos.exception.ApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;

import java.util.List;

@Tag(name = "User Management", description = "APIs for managing users")
@RestController
@RequestMapping("/api/user")
public class UserController {
    @Autowired
    private UserDto userDto;

    @Operation(summary = "Create a new user")
    @RequestMapping(path = "/add", method = RequestMethod.POST)
    public UserData create(@RequestBody UserForm userForm) throws ApiException {
        return userDto.create(userForm);
    }

    @Operation(summary = "Get all users with pagination")
    @RequestMapping(path = "/get-all-paginated", method = RequestMethod.POST)
    public Page<UserData> getAll(@RequestBody PageForm form) throws ApiException {
        return userDto.getAll(form);
    }

    @Operation(summary = "Get user by ID")
    @RequestMapping(path = "/get-by-id/{id}", method = RequestMethod.GET)
    public UserData getById(@PathVariable String id) throws ApiException {
        return userDto.getById(id);
    }
}
