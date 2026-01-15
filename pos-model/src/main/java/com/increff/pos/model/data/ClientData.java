package com.increff.pos.model.data;

import lombok.Data;

/**
 * Data transfer object for Client
 */
@Data
public class ClientData {
    private String id;
    private String clientId;
    private String name;
    private String phone;
    private String email;

}
