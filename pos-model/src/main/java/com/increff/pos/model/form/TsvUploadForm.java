package com.increff.pos.model.form;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TsvUploadForm {
    private String fileContent; // Base64 encoded TSV content
}
