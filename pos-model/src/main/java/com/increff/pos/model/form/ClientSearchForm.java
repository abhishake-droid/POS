package com.increff.pos.model.form;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClientSearchForm {
    private Integer page;
    private Integer size;
    private String clientId;
    private String name;
    private String email;
}
