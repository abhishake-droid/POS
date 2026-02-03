package com.increff.invoice.service;

import com.increff.invoice.exception.InvoiceException;
import com.increff.invoice.model.InvoiceLineItem;
import com.increff.invoice.model.InvoiceRequest;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Apache FOP-based PDF generator(returns Base64).
 */
public class InvoiceGenerator {

    private final FopFactory fopFactory;
    private final TransformerFactory transformerFactory;

    public InvoiceGenerator() {
        try {
            this.fopFactory = FopFactory.newInstance(new java.io.File(".").toURI());
            this.transformerFactory = TransformerFactory.newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize FOP", e);
        }
    }

    public String generateInvoicePdfBase64(InvoiceRequest request) throws InvoiceException {
        try {
            String xml = buildSimpleXml(request);
            InputStream xmlInput = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));

            Source src = new StreamSource(xmlInput);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            FOUserAgent foUserAgent = fopFactory.newFOUserAgent();
            Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, foUserAgent, out);

            Transformer transformer = transformerFactory.newTransformer();
            Result res = new SAXResult(fop.getDefaultHandler());

            transformer.transform(src, res);

            byte[] pdfBytes = out.toByteArray();
            return Base64.getEncoder().encodeToString(pdfBytes);
        } catch (Exception e) {
            throw new InvoiceException("Failed to generate invoice PDF: " + e.getMessage(), e);
        }
    }

    // Build a reasonably styled XSL‑FO invoice layout
    private String buildSimpleXml(InvoiceRequest request) {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
                .append("<fo:root xmlns:fo=\"http://www.w3.org/1999/XSL/Format\">")
                .append("<fo:layout-master-set>")
                .append("<fo:simple-page-master master-name=\"simple\" page-height=\"29.7cm\" page-width=\"21cm\" margin-top=\"1.5cm\" margin-bottom=\"2cm\" margin-left=\"2cm\" margin-right=\"2cm\">")
                .append("<fo:region-body margin-top=\"3cm\" margin-bottom=\"1.5cm\"/>")
                .append("<fo:region-before extent=\"3cm\"/>")
                .append("<fo:region-after extent=\"1.5cm\"/>")
                .append("</fo:simple-page-master>")
                .append("</fo:layout-master-set>")
                .append("<fo:page-sequence master-reference=\"simple\">")
                // Header
                .append("<fo:static-content flow-name=\"xsl-region-before\">")
                .append("<fo:block font-family=\"Helvetica\" font-size=\"18pt\" font-weight=\"bold\" color=\"#1a237e\" space-after=\"4pt\">")
                .append("INVOICE")
                .append("</fo:block>")
                .append("<fo:block font-family=\"Helvetica\" font-size=\"9pt\" color=\"#555555\" space-after=\"2pt\">")
                .append("POS System")
                .append("</fo:block>")
                .append("<fo:block font-family=\"Helvetica\" font-size=\"8pt\" color=\"#777777\" space-after=\"8pt\">")
                .append("Thank you for your business.")
                .append("</fo:block>")
                .append("<fo:block font-family=\"Helvetica\" font-size=\"9pt\">")
                .append("<fo:inline font-weight=\"bold\">Invoice: </fo:inline>")
                .append(escape(request.getInvoiceId() != null ? request.getInvoiceId() : ""))
                .append("</fo:block>")
                .append("<fo:block font-family=\"Helvetica\" font-size=\"9pt\">")
                .append("<fo:inline font-weight=\"bold\">Order: </fo:inline>")
                .append(escape(request.getOrderId() != null ? request.getOrderId() : ""))
                .append("</fo:block>")
                .append("<fo:block font-family=\"Helvetica\" font-size=\"9pt\">")
                .append("<fo:inline font-weight=\"bold\">Date: </fo:inline>")
                .append(formatDate(request.getOrderDate()))
                .append("</fo:block>")
                .append("</fo:static-content>")

                // Footer
                .append("<fo:static-content flow-name=\"xsl-region-after\">")
                .append("<fo:block font-family=\"Helvetica\" font-size=\"8pt\" color=\"#999999\" text-align=\"center\">")
                .append("This is a system generated invoice. No signature required.")
                .append("</fo:block>")
                .append("</fo:static-content>")

                // Body
                .append("<fo:flow flow-name=\"xsl-region-body\">")

                // Billing section
                .append("<fo:block font-family=\"Helvetica\" font-size=\"10pt\" space-after=\"2pt\">")
                .append(escape(request.getCustomerName()))
                .append("</fo:block>")
                .append("<fo:block font-family=\"Helvetica\" font-size=\"9pt\" color=\"#666666\" space-after=\"12pt\">")
                .append(escape(request.getBillingAddress() != null ? request.getBillingAddress() : ""))
                .append("</fo:block>")

                // Line items table
                .append("<fo:block font-family=\"Helvetica\" font-size=\"11pt\" font-weight=\"bold\" space-after=\"6pt\">")
                .append("Items")
                .append("</fo:block>")
                .append("<fo:table table-layout=\"fixed\" width=\"100%\" border-collapse=\"separate\" border-separation=\"2pt\">")
                .append("<fo:table-column column-width=\"1.2cm\"/>")
                .append("<fo:table-column column-width=\"3cm\"/>")
                .append("<fo:table-column column-width=\"7cm\"/>")
                .append("<fo:table-column column-width=\"2cm\"/>")
                .append("<fo:table-column column-width=\"2.5cm\"/>")
                .append("<fo:table-column column-width=\"3cm\"/>")

                // Table header
                .append("<fo:table-header>")
                .append("<fo:table-row background-color=\"#e3f2fd\">")
                .append("<fo:table-cell padding=\"4pt\"><fo:block font-size=\"9pt\" font-family=\"Helvetica\" font-weight=\"bold\">#</fo:block></fo:table-cell>")
                .append("<fo:table-cell padding=\"4pt\"><fo:block font-size=\"9pt\" font-family=\"Helvetica\" font-weight=\"bold\">SKU</fo:block></fo:table-cell>")
                .append("<fo:table-cell padding=\"4pt\"><fo:block font-size=\"9pt\" font-family=\"Helvetica\" font-weight=\"bold\">Product</fo:block></fo:table-cell>")
                .append("<fo:table-cell padding=\"4pt\" text-align=\"center\"><fo:block font-size=\"9pt\" font-family=\"Helvetica\" font-weight=\"bold\">Qty</fo:block></fo:table-cell>")
                .append("<fo:table-cell padding=\"4pt\" text-align=\"right\"><fo:block font-size=\"9pt\" font-family=\"Helvetica\" font-weight=\"bold\">MRP</fo:block></fo:table-cell>")
                .append("<fo:table-cell padding=\"4pt\" text-align=\"right\"><fo:block font-size=\"9pt\" font-family=\"Helvetica\" font-weight=\"bold\">Line Total</fo:block></fo:table-cell>")
                .append("</fo:table-row>")
                .append("</fo:table-header>")

                // Table body
                .append("<fo:table-body>");

        java.util.List<InvoiceLineItem> items = request.getItems();
        if (items != null && !items.isEmpty()) {
            int index = 1;
            for (InvoiceLineItem item : items) {
                sb.append("<fo:table-row>")
                        .append("<fo:table-cell padding=\"3pt\"><fo:block font-size=\"9pt\" font-family=\"Helvetica\">")
                        .append(index)
                        .append("</fo:block></fo:table-cell>")
                        .append("<fo:table-cell padding=\"3pt\"><fo:block font-size=\"9pt\" font-family=\"Helvetica\">")
                        .append(escape(item.getSku()))
                        .append("</fo:block></fo:table-cell>")
                        .append("<fo:table-cell padding=\"3pt\"><fo:block font-size=\"9pt\" font-family=\"Helvetica\">")
                        .append(escape(item.getProductName()))
                        .append("</fo:block></fo:table-cell>")
                        .append("<fo:table-cell padding=\"3pt\" text-align=\"center\"><fo:block font-size=\"9pt\" font-family=\"Helvetica\">")
                        .append(item.getQuantity() != null ? item.getQuantity() : 0)
                        .append("</fo:block></fo:table-cell>")
                        .append("<fo:table-cell padding=\"3pt\" text-align=\"right\"><fo:block font-size=\"9pt\" font-family=\"Helvetica\">")
                        .append(item.getMrp() != null ? item.getMrp() : 0.0)
                        .append("</fo:block></fo:table-cell>")
                        .append("<fo:table-cell padding=\"3pt\" text-align=\"right\"><fo:block font-size=\"9pt\" font-family=\"Helvetica\">")
                        .append(item.getLineTotal() != null ? item.getLineTotal() : 0.0)
                        .append("</fo:block></fo:table-cell>")
                        .append("</fo:table-row>");
                index++;
            }
        } else {
            sb.append("<fo:table-row>")
                    .append("<fo:table-cell padding=\"4pt\" number-columns-spanned=\"6\">")
                    .append("<fo:block font-size=\"9pt\" font-family=\"Helvetica\" font-style=\"italic\" color=\"#777777\">")
                    .append("No items")
                    .append("</fo:block>")
                    .append("</fo:table-cell>")
                    .append("</fo:table-row>");
        }

        sb.append("</fo:table-body>")
                .append("</fo:table>")

                // Summary section
                .append("<fo:block space-before=\"12pt\"/>")
                .append("<fo:block font-family=\"Helvetica\" font-size=\"10pt\" text-align=\"right\">")
                .append("<fo:inline font-weight=\"bold\">Subtotal: </fo:inline>")
                .append(request.getSubTotal() != null ? request.getSubTotal() : request.getTotalAmount())
                .append("</fo:block>")
                .append("<fo:block font-family=\"Helvetica\" font-size=\"10pt\" text-align=\"right\">")
                .append("<fo:inline font-weight=\"bold\">Tax: </fo:inline>")
                .append(request.getTaxAmount() != null ? request.getTaxAmount() : 0.0)
                .append("</fo:block>")
                .append("<fo:block font-family=\"Helvetica\" font-size=\"11pt\" font-weight=\"bold\" text-align=\"right\" space-before=\"3pt\">")
                .append("<fo:inline>Total: </fo:inline>")
                .append(request.getTotalAmount() != null ? request.getTotalAmount() : 0.0)
                .append("</fo:block>")

                .append("</fo:flow>")
                .append("</fo:page-sequence>")
                .append("</fo:root>");
        return sb.toString();
    }

    private String formatDate(java.time.ZonedDateTime dateTime) {
        if (dateTime == null)
            return "";

        java.time.format.DateTimeFormatter dayFormatter = java.time.format.DateTimeFormatter.ofPattern("d");
        java.time.format.DateTimeFormatter monthFormatter = java.time.format.DateTimeFormatter.ofPattern("MMM");
        java.time.format.DateTimeFormatter yearFormatter = java.time.format.DateTimeFormatter.ofPattern("yyyy");
        java.time.format.DateTimeFormatter timeFormatter = java.time.format.DateTimeFormatter.ofPattern("HH:mm");

        int day = Integer.parseInt(dateTime.format(dayFormatter));
        String month = dateTime.format(monthFormatter);
        String year = dateTime.format(yearFormatter);
        String time = dateTime.format(timeFormatter);

        String dayWithSuffix = day + getDayOfMonthSuffix(day);

        return dayWithSuffix + " " + month + " " + year + " at " + time;
    }

    private String getDayOfMonthSuffix(int day) {
        if (day >= 11 && day <= 13) {
            return "th";
        }
        switch (day % 10) {
            case 1:
                return "st";
            case 2:
                return "nd";
            case 3:
                return "rd";
            default:
                return "th";
        }
    }

    private String escape(String v) {
        if (v == null)
            return "";
        return v.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
