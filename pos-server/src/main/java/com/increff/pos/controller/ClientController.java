package com.increff.pos.controller;

import com.increff.pos.dto.ClientDto;
import com.increff.pos.model.data.ClientData;
import com.increff.pos.model.form.ClientForm;
import com.increff.pos.model.form.PageForm;
import com.increff.pos.exception.ApiException;
import com.increff.pos.util.AuthUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

@Tag(name = "Client Management", description = "APIs for managing clients")
@RestController
@RequestMapping("/api/client")
public class ClientController {

    private final ClientDto clientDto;
    private final AuthUtil authUtil;

    public ClientController(ClientDto clientDto, AuthUtil authUtil) {

        this.clientDto = clientDto;
        this.authUtil = authUtil;
    }

    @Operation(summary = "Create a new client")
    @PostMapping("/add")
    public ClientData create(@RequestBody ClientForm form) throws ApiException {
        return clientDto.create(form);
    }

    @Operation(summary = "Get all clients with pagination")
    @PostMapping("/get-all-paginated")
    public Page<ClientData> getAll(@RequestBody PageForm form) throws ApiException {
        return clientDto.getAll(form);
    }

    @Operation(summary = "Get client by ID")
    @GetMapping("/get-by-id/{clientId}")
    public ClientData getById(@PathVariable String clientId) throws ApiException {
        return clientDto.getById(clientId);
    }

    @Operation(summary = "Update client")
    @PutMapping("/update/{id}")
    public ClientData update(@PathVariable String id, @RequestBody ClientForm form, HttpServletRequest request) throws ApiException {
        if (!authUtil.isSupervisor(request)) {
            throw new ApiException("Only supervisors can update clients");
        }
        return clientDto.update(id, form);
    }
}
