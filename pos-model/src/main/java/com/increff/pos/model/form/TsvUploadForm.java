package com.increff.pos.model.form;

import lombok.Data;

/**
 * Form for TSV file upload (base64 encoded)
 */
@Data
public class TsvUploadForm {
    private String fileContent; // Base64 encoded TSV content
}
