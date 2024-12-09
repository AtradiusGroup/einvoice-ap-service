package com.atradius.einvoice.ap.service;

import com.atradius.einvoice.ap.exception.PdfCreateException;
import com.atradius.einvoice.ap.model.EinvoiceVariables;
import com.atradius.einvoice.ap.model.InvoiceData;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.core.util.FileUtils;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

@Component
public class PdfCreateProcessor implements UblProcessor{
    private PdfMappingData pdfMappingData;
    private UblXmlReader xmlReader;

    public PdfCreateProcessor(PdfMappingData pdfMappingData, UblXmlReader xmlReader){
        this.pdfMappingData = pdfMappingData;
        this.xmlReader = xmlReader;
    }
    @Override
    @Retryable(retryFor = Exception.class, maxAttemptsExpression = "${services.retryMaxAttempts}",
            backoff = @Backoff(delayExpression = "${services.retryInvoiceTimer}"))
    public void process(EinvoiceVariables variables, InvoiceData data) throws PdfCreateException {
        try(ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter pdfWriter = PdfWriter.getInstance(document, baos);
            document.open();
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, BaseColor.BLACK);
            Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 12, BaseColor.BLACK);

            variables.setDocumentType(getDocumentType(data.getUblContent()));
            String rootElement = "INVOICE".equalsIgnoreCase(variables.getDocumentType()) ? "/ns0:Invoice" : "/ns0:CreditNote";
            addParagraphText(document, headerFont, variables.getDocumentType(), Element.ALIGN_CENTER);
            document.add(Chunk.NEWLINE);

            List<String> supplierData = pdfMappingData.getSupplierData(data.getUblContent(), rootElement);
            List<String> invoiceData = pdfMappingData.getInvoiceData(data.getUblContent(), rootElement);
            document.add(addTableData(data.getUblContent(), supplierData, invoiceData, rootElement));
            document.add(Chunk.NEWLINE);

            List<String> customerData = pdfMappingData.getCustomerData(data.getUblContent(), rootElement);
            List<String> bankData = pdfMappingData.getBankData(data.getUblContent(), rootElement);
            document.add(addTableData(data.getUblContent(), customerData, bankData, rootElement));
            document.add(Chunk.NEWLINE);

            PdfPTable paymentsTable = new PdfPTable(5);
            paymentsTable.setWidthPercentage(100f);
            // Add PDF Table Header ->
            Stream.of("ID", "Description", "Qunatity", "Price", "Tax")
                    .forEach(headerTitle -> { paymentsTable.addCell(addCell(paymentsTable, headerTitle, Element.ALIGN_CENTER)); });
            List<List<String>> payments = pdfMappingData.getPaymentsData(data.getUblContent(), rootElement);
            payments.stream().forEach(paymentItem -> {
                paymentItem.stream().forEach(item -> {
                    try {
                        String content = StringUtils.isNotEmpty(item) ? xmlReader.getXPathValue(data.getUblContent(), item) : "";
                        paymentsTable.addCell(addCell(paymentsTable,content, Element.ALIGN_CENTER));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
            });
            document.add(paymentsTable);
            document.close();
            data.setPdfContents(baos.toByteArray());
        }catch (Exception e){
            throw new PdfCreateException("Failed to create pdf", e);
        }
    }

    private PdfPTable addTableData(String xml, List<String> left, List<String> right, String rootElement) throws Exception{
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100f);
        int maxRows = left.size() > right.size() ? left.size() : right.size();
        for(int i = 0; i < maxRows; i++) {
            table.addCell(addCellWithoutBorder(xml, table, getListItem(left, i), Element.ALIGN_LEFT));
            table.addCell(addCellWithoutBorder(xml, table, getListItem(right, i), Element.ALIGN_RIGHT));
        }
        return table;
    }

    private String getListItem(List<String> items, int index){
        return index < items.size() ? items.get(index) : "";
    }

    private void addParagraphText(Document document, Font font, String text, int alignment)throws DocumentException{
        Paragraph invoiceTxtPara = new Paragraph( text, font);
        invoiceTxtPara.setAlignment(alignment);
        document.add(invoiceTxtPara);
    }

    private PdfPCell addCellWithoutBorder(String xml, PdfPTable table, String content, int alignment)throws Exception{
        PdfPCell cell = addCell(table, content, alignment);
        cell.setBorder(PdfPCell.NO_BORDER);
        return cell;
    }
    private PdfPCell addCell(PdfPTable table, String content, int alignment){
        PdfPCell cell = new PdfPCell(new Phrase(content));
        cell.setHorizontalAlignment(alignment);
        cell.setVerticalAlignment(alignment);
        return cell;
    }
    private String getDocumentType(String ublXml) throws Exception {
        String documentType = null;
        if (xmlReader.getElementValue(ublXml, "cbc:InvoiceTypeCode", null) != null) {
            documentType = "INVOICE";
        } else if (xmlReader.getElementValue(ublXml, "cbc:CreditNoteTypeCode", null) != null) {
            documentType = "CREDIT_NOTE";
        } else {
            documentType = "UNKOWN";
        }
        return documentType;
    }
}
