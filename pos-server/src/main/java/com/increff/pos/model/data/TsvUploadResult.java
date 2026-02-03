package com.increff.pos.model.data;

public class TsvUploadResult {
    private int rowNumber;
    private String status;
    private String errorMessage;
    private String data;

    public TsvUploadResult() {
    }

    public TsvUploadResult(int rowNumber, String status, String errorMessage, String data) {
        this.rowNumber = rowNumber;
        this.status = status;
        this.errorMessage = errorMessage;
        this.data = data;
    }

    public int getRowNumber() {
        return rowNumber;
    }

    public void setRowNumber(int rowNumber) {
        this.rowNumber = rowNumber;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
