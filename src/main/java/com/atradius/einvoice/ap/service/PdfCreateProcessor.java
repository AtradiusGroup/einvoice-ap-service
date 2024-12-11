package com.atradius.einvoice.ap.service;

import com.atradius.einvoice.ap.config.APConfig;
import com.atradius.einvoice.ap.exception.PdfCreateException;
import com.atradius.einvoice.ap.model.EinvoiceVariables;
import com.atradius.einvoice.ap.model.InvoiceData;
import com.itextpdf.io.font.FontConstants;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.border.Border;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.property.HorizontalAlignment;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.layout.property.UnitValue;
import com.itextpdf.layout.property.VerticalAlignment;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.ResourceLoader;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.stream.Stream;

@Component
public class PdfCreateProcessor implements UblProcessor{
    private PdfMappingData pdfMappingData;
    private UblXmlReader xmlReader;
    private APConfig config;
    private ResourceLoader resourceLoader;

    public PdfCreateProcessor(PdfMappingData pdfMappingData, UblXmlReader xmlReader, APConfig config,
                              ResourceLoader resourceLoader){
        this.pdfMappingData = pdfMappingData;
        this.xmlReader = xmlReader;
        this.config = config;
        this.resourceLoader = resourceLoader;
    }
    @Override
    @Retryable(retryFor = Exception.class, maxAttemptsExpression = "${services.retryMaxAttempts}",
            backoff = @Backoff(delayExpression = "${services.retryInvoiceTimer}"))
    public void process(EinvoiceVariables variables, InvoiceData data) throws PdfCreateException {
        try(ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter pdfWriter = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(pdfWriter);
            Document document = new Document(pdf);

            Image logo = new Image(ImageDataFactory.create(resourceLoader.getResource("classpath:logoAtradius_tagline_red.PNG").getURL()));
            PdfFont font = PdfFontFactory.createFont(FontConstants.HELVETICA_BOLD);

            Table headerTable = new Table(2);
            headerTable.setWidth(UnitValue.createPercentValue(100));
            headerTable.addCell(new Cell(1, 1)
                    .add(logo.scaleToFit(200.0f, 400.0f))
                    .setBorder(Border.NO_BORDER)
                    .setHorizontalAlignment(HorizontalAlignment.LEFT));

            variables.setDocumentType(getDocumentType(data.getUblContent()));
            String rootElement = "INVOICE".equalsIgnoreCase(variables.getDocumentType()) ? "/ns0:Invoice" : "/ns1:CreditNote";
            headerTable.addCell(new Cell(1, 1)
                    .add(addParagraphText(font, variables.getDocumentType(), HorizontalAlignment.RIGHT))
                    .setBorder(Border.NO_BORDER)
                    .setVerticalAlignment(VerticalAlignment.MIDDLE));
            document.add(headerTable);
            addNewLine(document);

            List<String> supplierData = pdfMappingData.getSupplierData(data.getUblContent(), rootElement);
            List<String> invoiceData = pdfMappingData.getInvoiceData(data.getUblContent(), rootElement);
            document.add(addTableData(supplierData, invoiceData));
            addNewLine(document);

            List<String> customerData = pdfMappingData.getCustomerData(data.getUblContent(), rootElement);
            List<String> bankData = pdfMappingData.getBankData(data.getUblContent(), rootElement);
            document.add(addTableData(customerData, bankData));
            addNewLine(document);

            Table paymentsTable = new Table(5);
            paymentsTable.setWidth(UnitValue.createPercentValue(100)).setTextAlignment(TextAlignment.CENTER)
                    .setHorizontalAlignment(HorizontalAlignment.CENTER);
            // Add PDF Table Header ->
            Stream.of("ID", "Description", "Qunatity", "Price", "Tax")
                    .forEach(headerTitle -> { paymentsTable.addHeaderCell(new Cell().add(headerTitle).setFont(font)); });
            List<List<String>> payments = pdfMappingData.getPaymentsData(data.getUblContent(), rootElement);
            payments.stream().forEach(paymentItem -> {
                paymentItem.stream().forEach(item -> {
                    try {
                        String content = StringUtils.isNotEmpty(item) ? xmlReader.getXPathValue(data.getUblContent(), item) : "";
                        paymentsTable.addCell(content);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
            });
            paymentsTable.addCell(addCell("Total", HorizontalAlignment.RIGHT, 1, 4).setFont(font));
            String total = xmlReader.getXPathValue(data.getUblContent(), rootElement + config.getTotalAmountPath());
            paymentsTable.addCell(addCell(total, HorizontalAlignment.CENTER, 1, 1));
            document.add(paymentsTable);
            document.close();
            data.setPdfContents(baos.toByteArray());
        }catch (Exception e){
            throw new PdfCreateException("Failed to create pdf", e);
        }
    }

    private Table addTableData(List<String> left, List<String> right) throws Exception{
        Table table = new Table(2);
        table.setWidth(UnitValue.createPercentValue(100));
        int maxRows = left.size() > right.size() ? left.size() : right.size();
        for(int i = 0; i < maxRows; i++) {
            table.addCell(addCellWithoutBorder(getListItem(left, i), HorizontalAlignment.LEFT, 1, 1));
            table.addCell(addCellWithoutBorder(getListItem(right, i), HorizontalAlignment.RIGHT, 1, 1));
        }
        return table;
    }

    private String getListItem(List<String> items, int index){
        return index < items.size() ? items.get(index) : "";
    }

    private Paragraph addParagraphText(PdfFont font, String text, HorizontalAlignment alignment){
        Paragraph para = new Paragraph( text).setFont(font);
        para.setHorizontalAlignment(alignment);
        return para;
    }

    private Cell addCellWithoutBorder(String content, HorizontalAlignment alignment, int rowspan, int colspan)throws Exception{
        Cell cell = addCell(content, alignment, rowspan, colspan);
        cell.setBorder(Border.NO_BORDER);
        return cell;
    }
    private Cell addCell(String content, HorizontalAlignment alignment, int rowspan, int colspan){
        Cell cell = new Cell(rowspan, colspan).add(new Paragraph(content));
        cell.setHorizontalAlignment(alignment);
        cell.setVerticalAlignment(VerticalAlignment.MIDDLE);
        cell.setFontSize(10);
        return cell;
    }
    private String getDocumentType(String ublXml) throws Exception {
        String documentType = null;
        if (xmlReader.getElementValue(ublXml, "cbc:InvoiceTypeCode", null) != null) {
            documentType = "INVOICE";
        } else if (xmlReader.getElementValue(ublXml, "cbc:CreditNoteTypeCode", null) != null) {
            documentType = "CREDITNOTE";
        } else {
            documentType = "UNKOWN";
        }
        return documentType;
    }

    private void addNewLine(Document document){
        document.add(new Paragraph().add(new Text("\n")));
    }
}
