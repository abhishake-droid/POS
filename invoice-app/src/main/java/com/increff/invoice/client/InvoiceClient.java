package com.increff.invoice.client;

import com.increff.invoice.model.InvoiceRequest;
import com.increff.invoice.db.InvoicePojo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class InvoiceClient {
    private static final Logger logger = LoggerFactory.getLogger(InvoiceClient.class);

    private final RestTemplate restTemplate;

    @Value("${invoice.service.url:http://localhost:8081}")
    private String invoiceServiceUrl;

    public InvoiceClient() {
        this.restTemplate = new RestTemplate();
    }

    public InvoicePojo generateInvoice(InvoiceRequest request) throws Exception {
        try {
            String url = invoiceServiceUrl + "/api/invoice/generate";
            logger.info("Calling invoice service at: {}", url);

            HttpEntity<InvoiceRequest> entity = new HttpEntity<>(request);
            ResponseEntity<InvoicePojo> response = restTemplate.postForEntity(url, entity, InvoicePojo.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody();
            } else {
                throw new Exception("Invoice service returned error: " + response.getStatusCode());
            }
        } catch (Exception e) {
            logger.error("Failed to call invoice service", e);
            throw new Exception("Failed to generate invoice: " + e.getMessage());
        }
    }

    public byte[] downloadInvoicePdf(String orderId) throws Exception {
        try {
            String url = invoiceServiceUrl + "/api/invoice/download/" + orderId;
            logger.info("Downloading invoice PDF from: {}", url);

            ResponseEntity<byte[]> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    byte[].class);

            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody();
            } else {
                throw new Exception("Failed to download invoice PDF");
            }
        } catch (Exception e) {
            logger.error("Failed to download invoice PDF", e);
            throw new Exception("Failed to download invoice: " + e.getMessage());
        }
    }
}
