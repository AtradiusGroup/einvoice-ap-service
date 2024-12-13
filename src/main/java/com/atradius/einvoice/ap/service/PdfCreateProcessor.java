package com.atradius.einvoice.ap.service;

import com.atradius.einvoice.ap.config.APConfig;
import com.atradius.einvoice.ap.model.EinvoiceVariables;
import com.atradius.einvoice.ap.model.InvoiceData;
import com.atradius.einvoice.ap.pdf.PdfControls;
import com.atradius.einvoice.ap.pdf.PdfTable;
import com.atradius.einvoice.ap.pdf.PdfTableCellData;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.core.io.ResourceLoader;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

@Service
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
    public void process(EinvoiceVariables variables, InvoiceData data) throws Exception {
        try(PDDocument document = new PDDocument(); ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PDType0Font regular = PDType0Font.load(document, resourceLoader.getResource("classpath:fonts/Arial.ttf").getInputStream());
            PDType0Font bold = PDType0Font.load(document, resourceLoader.getResource("classpath:fonts/Arial_Bold.ttf").getInputStream());
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            int lineNumber = 3;

            try(PDPageContentStream contents = new PDPageContentStream(document, page)) {
                PdfControls pdfControls = new PdfControls(contents, page.getMediaBox().getWidth(), page.getMediaBox().getHeight(), regular, bold);
                String logoPath = resourceLoader.getResource("classpath:logoAtradius_tagline_red.PNG").getFile().getPath();
                PDImageXObject logo = PDImageXObject.createFromFile(logoPath, document);
                pdfControls.addLogo(lineNumber, logo);

                variables.setDocumentType(getDocumentType(data.getUblContent()));
                String rootElement = "INVOICE".equalsIgnoreCase(variables.getDocumentType()) ? "/ns0:Invoice" : "/ns1:CreditNote";
                pdfControls.addHeaderText(lineNumber, variables.getDocumentType());
                lineNumber += 2;

                List<String> supplierData = pdfMappingData.getSupplierData(data.getUblContent(), rootElement);
                List<String> invoiceData = pdfMappingData.getInvoiceData(data.getUblContent(), rootElement);
                addTableData(pdfControls, supplierData, invoiceData, lineNumber);
                lineNumber += supplierData.size() > invoiceData.size() ? supplierData.size() : invoiceData.size() + 1;

                List<String> customerData = pdfMappingData.getCustomerData(data.getUblContent(), rootElement);
                List<String> bankData = pdfMappingData.getBankData(data.getUblContent(), rootElement);
                addTableData(pdfControls, customerData, bankData, lineNumber);
                lineNumber += customerData.size() > bankData.size() ? customerData.size() : bankData.size() + 1;

                List<List<String>> paymentsData = new ArrayList<>();
                paymentsData.add(List.of("ID", "Name", "Qty", "Tax", "Price"));
                paymentsData.addAll(pdfMappingData.getPaymentsData(data.getUblContent(), rootElement));
                PdfTable paymentTable = new PdfTable(PdfTableCellData.paymentTableCells, paymentsData);
                pdfControls.drawTable(paymentTable, lineNumber);
                lineNumber += paymentsData.size();

                List<List<String>> paymentTotalsData = new ArrayList<>();
                String total = xmlReader.getXPathValue(data.getUblContent(), rootElement + config.getTotalAmountPath());
                paymentTotalsData.add(List.of("Total", total));
                PdfTable totalTable = new PdfTable(PdfTableCellData.totalCells, paymentTotalsData);
                pdfControls.drawTable(totalTable, lineNumber);
                lineNumber += paymentTotalsData.size();

            }
            document.save(baos);
            data.setPdfContents(baos.toByteArray());
        }
    }

    private void addTableData(PdfControls pdfControls, List<String> left, List<String> right, int lineNumber) throws Exception{
        int maxRows = left.size() > right.size() ? left.size() : right.size();
        for(int i = 0; i < maxRows; i++) {
            pdfControls.addText(lineNumber, getListItem(left, i), true);
            pdfControls.addText(lineNumber, getListItem(right, i), false);
            lineNumber += 1;
        }
    }

    private String getListItem(List<String> items, int index){
        return index < items.size() ? items.get(index) : "";
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
}
