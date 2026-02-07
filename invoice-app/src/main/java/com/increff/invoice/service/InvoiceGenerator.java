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

    private String buildSimpleXml(InvoiceRequest request) {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
                .append("<fo:root xmlns:fo=\"http://www.w3.org/1999/XSL/Format\">")
                .append("<fo:layout-master-set>")
                .append("<fo:simple-page-master master-name=\"simple\" page-height=\"29.7cm\" page-width=\"21cm\" margin-top=\"1cm\" margin-bottom=\"1.5cm\" margin-left=\"2cm\" margin-right=\"2cm\">")
                .append("<fo:region-body margin-top=\"1cm\" margin-bottom=\"1.5cm\"/>")
                .append("<fo:region-before extent=\"1cm\"/>")
                .append("<fo:region-after extent=\"1.5cm\"/>")
                .append("</fo:simple-page-master>")
                .append("</fo:layout-master-set>")
                .append("<fo:page-sequence master-reference=\"simple\">")

                // Header - Simple and clean
                .append("<fo:static-content flow-name=\"xsl-region-before\">")
                .append("<fo:block font-family=\"Helvetica\" font-size=\"24pt\" font-weight=\"bold\" color=\"#1e40af\" text-align=\"center\">")
                .append("INVOICE")
                .append("</fo:block>")
                .append("</fo:static-content>")

                // Body
                .append("<fo:flow flow-name=\"xsl-region-body\">")

                // Invoice Details - Clean 2-column layout
                .append("<fo:table table-layout=\"fixed\" width=\"100%\" space-after=\"20pt\">")
                .append("<fo:table-column column-width=\"50%\"/>")
                .append("<fo:table-column column-width=\"50%\"/>")
                .append("<fo:table-body>")
                .append("<fo:table-row>")
                .append("<fo:table-cell padding=\"4pt\">")
                .append("<fo:block font-family=\"Helvetica\" font-size=\"9pt\" color=\"#64748b\">Invoice Number</fo:block>")
                .append("<fo:block font-family=\"Helvetica\" font-size=\"12pt\" font-weight=\"bold\" color=\"#0f172a\" space-after=\"8pt\">")
                .append(escape(request.getInvoiceId() != null ? request.getInvoiceId() : ""))
                .append("</fo:block>")
                .append("</fo:table-cell>")
                .append("<fo:table-cell padding=\"4pt\" text-align=\"right\">")
                .append("<fo:block font-family=\"Helvetica\" font-size=\"9pt\" color=\"#64748b\">Invoice Date</fo:block>")
                .append("<fo:block font-family=\"Helvetica\" font-size=\"11pt\" font-weight=\"600\" color=\"#0f172a\" space-after=\"8pt\">")
                .append(formatDate(request.getOrderDate()))
                .append("</fo:block>")
                .append("</fo:table-cell>")
                .append("</fo:table-row>")
                .append("<fo:table-row>")
                .append("<fo:table-cell padding=\"4pt\">")
                .append("<fo:block font-family=\"Helvetica\" font-size=\"9pt\" color=\"#64748b\">Order Number</fo:block>")
                .append("<fo:block font-family=\"Helvetica\" font-size=\"11pt\" font-weight=\"600\" color=\"#475569\">")
                .append(escape(request.getOrderId() != null ? request.getOrderId() : ""))
                .append("</fo:block>")
                .append("</fo:table-cell>")
                .append("<fo:table-cell padding=\"4pt\"><fo:block/></fo:table-cell>")
                .append("</fo:table-row>")
                .append("</fo:table-body>")
                .append("</fo:table>")

                // Line items table - Professional with proper column widths
                .append("<fo:table table-layout=\"fixed\" width=\"100%\" border=\"1pt solid #cbd5e1\" space-after=\"16pt\">")
                .append("<fo:table-column column-width=\"8%\"/>") // S. No - narrower
                .append("<fo:table-column column-width=\"20%\"/>") // SKU
                .append("<fo:table-column column-width=\"37%\"/>") // Product - wider
                .append("<fo:table-column column-width=\"10%\"/>") // Qty
                .append("<fo:table-column column-width=\"12%\"/>") // Price
                .append("<fo:table-column column-width=\"13%\"/>") // Amount

                // Table header - Clean blue background
                .append("<fo:table-header>")
                .append("<fo:table-row background-color=\"#eff6ff\" border-bottom=\"1pt solid #cbd5e1\">")
                .append("<fo:table-cell padding=\"8pt\" border-right=\"1pt solid #e2e8f0\"><fo:block font-size=\"9pt\" font-family=\"Helvetica\" font-weight=\"bold\" color=\"#1e40af\">S. No</fo:block></fo:table-cell>")
                .append("<fo:table-cell padding=\"8pt\" border-right=\"1pt solid #e2e8f0\"><fo:block font-size=\"9pt\" font-family=\"Helvetica\" font-weight=\"bold\" color=\"#1e40af\">SKU</fo:block></fo:table-cell>")
                .append("<fo:table-cell padding=\"8pt\" border-right=\"1pt solid #e2e8f0\"><fo:block font-size=\"9pt\" font-family=\"Helvetica\" font-weight=\"bold\" color=\"#1e40af\">Product</fo:block></fo:table-cell>")
                .append("<fo:table-cell padding=\"8pt\" text-align=\"center\" border-right=\"1pt solid #e2e8f0\"><fo:block font-size=\"9pt\" font-family=\"Helvetica\" font-weight=\"bold\" color=\"#1e40af\">Qty</fo:block></fo:table-cell>")
                .append("<fo:table-cell padding=\"8pt\" text-align=\"right\" border-right=\"1pt solid #e2e8f0\"><fo:block font-size=\"9pt\" font-family=\"Helvetica\" font-weight=\"bold\" color=\"#1e40af\">Price</fo:block></fo:table-cell>")
                .append("<fo:table-cell padding=\"8pt\" text-align=\"right\"><fo:block font-size=\"9pt\" font-family=\"Helvetica\" font-weight=\"bold\" color=\"#1e40af\">Amount</fo:block></fo:table-cell>")
                .append("</fo:table-row>")
                .append("</fo:table-header>")

                // Table body
                .append("<fo:table-body>");

        java.util.List<InvoiceLineItem> items = request.getItems();
        if (items != null && !items.isEmpty()) {
            int index = 1;
            for (InvoiceLineItem item : items) {
                String rowBg = (index % 2 == 0) ? "#f9fafb" : "#ffffff";
                sb.append("<fo:table-row background-color=\"" + rowBg + "\" border-bottom=\"1pt solid #e2e8f0\">")
                        .append("<fo:table-cell padding=\"8pt\" border-right=\"1pt solid #e2e8f0\"><fo:block font-size=\"9pt\" font-family=\"Helvetica\" color=\"#64748b\">")
                        .append(index)
                        .append("</fo:block></fo:table-cell>")
                        .append("<fo:table-cell padding=\"8pt\" border-right=\"1pt solid #e2e8f0\"><fo:block font-size=\"9pt\" font-family=\"Helvetica\" color=\"#475569\">")
                        .append(escape(item.getSku()))
                        .append("</fo:block></fo:table-cell>")
                        .append("<fo:table-cell padding=\"8pt\" border-right=\"1pt solid #e2e8f0\"><fo:block font-size=\"9pt\" font-family=\"Helvetica\" color=\"#0f172a\">")
                        .append(escape(item.getProductName()))
                        .append("</fo:block></fo:table-cell>")
                        .append("<fo:table-cell padding=\"8pt\" text-align=\"center\" border-right=\"1pt solid #e2e8f0\"><fo:block font-size=\"9pt\" font-family=\"Helvetica\" font-weight=\"600\" color=\"#0f172a\">")
                        .append(item.getQuantity() != null ? item.getQuantity() : 0)
                        .append("</fo:block></fo:table-cell>")
                        .append("<fo:table-cell padding=\"8pt\" text-align=\"right\" border-right=\"1pt solid #e2e8f0\"><fo:block font-size=\"9pt\" font-family=\"Helvetica\" color=\"#475569\">")
                        .append(item.getMrp() != null ? String.format("%.2f", item.getMrp()) : "0.00")
                        .append("</fo:block></fo:table-cell>")
                        .append("<fo:table-cell padding=\"8pt\" text-align=\"right\"><fo:block font-size=\"9pt\" font-family=\"Helvetica\" font-weight=\"bold\" color=\"#0f172a\">")
                        .append(item.getLineTotal() != null ? String.format("%.2f", item.getLineTotal()) : "0.00")
                        .append("</fo:block></fo:table-cell>")
                        .append("</fo:table-row>");
                index++;
            }
        } else {
            sb.append("<fo:table-row>")
                    .append("<fo:table-cell padding=\"12pt\" number-columns-spanned=\"6\">")
                    .append("<fo:block font-size=\"9pt\" font-family=\"Helvetica\" font-style=\"italic\" color=\"#94a3b8\" text-align=\"center\">")
                    .append("No items")
                    .append("</fo:block>")
                    .append("</fo:table-cell>")
                    .append("</fo:table-row>");
        }

        sb.append("</fo:table-body>")
                .append("</fo:table>")

                // Summary section - Right-aligned, clean
                .append("<fo:table table-layout=\"fixed\" width=\"100%\">")
                .append("<fo:table-column column-width=\"proportional-column-width(2)\"/>")
                .append("<fo:table-column/>")
                .append("<fo:table-body>")

                // Subtotal
                .append("<fo:table-row>")
                .append("<fo:table-cell padding=\"6pt\"><fo:block/></fo:table-cell>")
                .append("<fo:table-cell padding=\"6pt\" border-top=\"1pt solid #cbd5e1\">")
                .append("<fo:block font-family=\"Helvetica\" font-size=\"10pt\" text-align=\"right\">")
                .append("<fo:inline color=\"#64748b\">Subtotal: </fo:inline>")
                .append("<fo:inline font-weight=\"bold\" color=\"#0f172a\">")
                .append(request.getSubTotal() != null ? String.format("%.2f", request.getSubTotal())
                        : String.format("%.2f", request.getTotalAmount()))
                .append("</fo:inline>")
                .append("</fo:block>")
                .append("</fo:table-cell>")
                .append("</fo:table-row>")

                // Tax
                .append("<fo:table-row>")
                .append("<fo:table-cell padding=\"6pt\"><fo:block/></fo:table-cell>")
                .append("<fo:table-cell padding=\"6pt\">")
                .append("<fo:block font-family=\"Helvetica\" font-size=\"10pt\" text-align=\"right\">")
                .append("<fo:inline color=\"#64748b\">Tax: </fo:inline>")
                .append("<fo:inline font-weight=\"bold\" color=\"#0f172a\">")
                .append(request.getTaxAmount() != null ? String.format("%.2f", request.getTaxAmount()) : "0.00")
                .append("</fo:inline>")
                .append("</fo:block>")
                .append("</fo:table-cell>")
                .append("</fo:table-row>")

                // Total - Highlighted with dynamic width
                .append("<fo:table-row>")
                .append("<fo:table-cell padding=\"10pt\"><fo:block/></fo:table-cell>")
                .append("<fo:table-cell padding=\"12pt 20pt\" background-color=\"#eff6ff\" border=\"3pt solid #3b82f6\">")
                .append("<fo:block font-family=\"Helvetica\" font-size=\"14pt\" font-weight=\"bold\" text-align=\"right\" white-space=\"nowrap\">")
                .append("<fo:inline color=\"#1e40af\">TOTAL: </fo:inline>")
                .append("<fo:inline color=\"#1e40af\">")
                .append(request.getTotalAmount() != null ? String.format("%.2f", request.getTotalAmount()) : "0.00")
                .append("</fo:inline>")
                .append("</fo:block>")
                .append("</fo:table-cell>")
                .append("</fo:table-row>")

                .append("</fo:table-body>")
                .append("</fo:table>")

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
