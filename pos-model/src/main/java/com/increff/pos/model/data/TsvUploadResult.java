package com.increff.pos.model.data;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TsvUploadResult {
    private int rowNumber;
    private String status; // "SUCCESS" or "FAILED"
    private String errorMessage;
    private String data; // Original row data or processed data
    
    public TsvUploadResult(int rowNumber, String status, String errorMessage, String data) {
        this.rowNumber = rowNumber;
        this.status = status;
        this.errorMessage = errorMessage;
        this.data = data;
    }
}
