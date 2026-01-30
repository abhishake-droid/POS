package com.increff.invoice.client;

import com.increff.invoice.model.InvoiceRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
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

    public byte[] generateInvoicePdf(InvoiceRequest request) throws Exception {
        try {
            String url = invoiceServiceUrl + "/api/invoice/generate";
            logger.info("Calling invoice service at: {}", url);

            HttpEntity<InvoiceRequest> entity = new HttpEntity<>(request);
            ResponseEntity<byte[]> response = restTemplate.postForEntity(url, entity, byte[].class);

            if (response.getStatusCode().is2xxSuccessful()) {
                logger.info("Received PDF ({} bytes) from invoice service", response.getBody().length);
                return response.getBody();
            } else {
                throw new Exception("Invoice service returned error: " + response.getStatusCode());
            }
        } catch (Exception e) {
            logger.error("Failed to call invoice service", e);
            throw new Exception("Failed to generate invoice PDF: " + e.getMessage());
        }
    }
}
