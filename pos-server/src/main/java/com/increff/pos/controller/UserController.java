package com.increff.pos.controller;

import com.increff.pos.model.data.UserData;
import com.increff.pos.model.form.UserForm;
import com.increff.pos.model.form.PageForm;
import com.increff.pos.dto.UserDto;
import com.increff.pos.exception.ApiException;
import com.increff.pos.util.AuthUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

@Tag(name = "Operator Management", description = "APIs for managing operators (Supervisor only)")
@RestController
@RequestMapping("/api/user")
public class UserController {
    @Autowired
    private UserDto userDto;

    @Autowired
    private AuthUtil authUtil;

    @Operation(summary = "Create a new operator (Supervisor only)")
    @RequestMapping(path = "/add", method = RequestMethod.POST)
    public UserData create(@RequestBody UserForm userForm, HttpServletRequest request) throws ApiException {
        if (!authUtil.isSupervisor(request)) {
            throw new ApiException("Only supervisors can create operators");
        }
        return userDto.create(userForm);
    }

    @Operation(summary = "Get all operators with pagination (Supervisor only)")
    @RequestMapping(path = "/get-all-paginated", method = RequestMethod.POST)
    public Page<UserData> getAll(@RequestBody PageForm form, HttpServletRequest request) throws ApiException {
        if (!authUtil.isSupervisor(request)) {
            throw new ApiException("Only supervisors can view operators");
        }
        return userDto.getAll(form);
    }

    @Operation(summary = "Get operator by ID (Supervisor only)")
    @RequestMapping(path = "/get-by-id/{id}", method = RequestMethod.GET)
    public UserData getById(@PathVariable String id, HttpServletRequest request) throws ApiException {
        if (!authUtil.isSupervisor(request)) {
            throw new ApiException("Only supervisors can view operators");
        }
        return userDto.getById(id);
    }
}
