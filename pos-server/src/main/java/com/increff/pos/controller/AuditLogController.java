package com.increff.pos.controller;

import com.increff.pos.dto.AuditLogDto;
import com.increff.pos.model.data.AuditLogData;
import com.increff.pos.model.form.PageForm;
import com.increff.pos.exception.ApiException;
import com.increff.pos.util.AuthUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

@Tag(name = "Audit Log Management", description = "APIs for viewing operator activity logs")
@RestController
@RequestMapping("/api/audit-log")
public class AuditLogController {
    private final AuditLogDto auditLogDto;
    private final AuthUtil authUtil;

    public AuditLogController(AuditLogDto auditLogDto, AuthUtil authUtil) {
        this.auditLogDto = auditLogDto;
        this.authUtil = authUtil;
    }

    @Operation(summary = "Get all audit logs with pagination (Supervisor only)")
    @PostMapping("/get-all-paginated")
    public Page<AuditLogData> getAll(@RequestBody PageForm form, HttpServletRequest request) throws ApiException {
        if (!authUtil.isSupervisor(request)) {
            throw new ApiException("Only supervisors can view audit logs");
        }
        return auditLogDto.getAll(form);
    }

    @Operation(summary = "Get all audit logs (Supervisor only)")
    @GetMapping("/get-all")
    public List<AuditLogData> getAll(HttpServletRequest request) throws ApiException {
        if (!authUtil.isSupervisor(request)) {
            throw new ApiException("Only supervisors can view audit logs");
        }
        return auditLogDto.getAll();
    }

    @Operation(summary = "Get audit logs by operator email (Supervisor only)")
    @GetMapping("/get-by-operator/{operatorEmail}")
    public List<AuditLogData> getByOperator(@PathVariable String operatorEmail, HttpServletRequest request) throws ApiException {
        if (!authUtil.isSupervisor(request)) {
            throw new ApiException("Only supervisors can view audit logs");
        }
        return auditLogDto.getByOperatorEmail(operatorEmail);
    }
}
