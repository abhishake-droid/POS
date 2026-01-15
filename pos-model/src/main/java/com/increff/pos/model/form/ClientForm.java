package com.increff.pos.model.form;

import lombok.Data;

/**
 * Form for creating/updating a client
 */
@Data
public class ClientForm {
//    private String id; // Client ID
    private String name; // Client name
    private String phone;
    private String email;

}
