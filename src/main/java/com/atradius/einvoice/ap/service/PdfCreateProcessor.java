package com.atradius.einvoice.ap.service;

import com.atradius.einvoice.ap.config.APConfig;
import com.atradius.einvoice.ap.model.EinvoiceVariables;
import com.atradius.einvoice.ap.model.InvoiceData;
import com.atradius.einvoice.ap.pdf.PdfCell;
import com.atradius.einvoice.ap.pdf.PdfConstants;
import com.atradius.einvoice.ap.pdf.PdfControls;
import com.atradius.einvoice.ap.pdf.PdfTable;
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
import java.io.File;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static com.atradius.einvoice.ap.pdf.PdfCell.LEFT;
import static com.atradius.einvoice.ap.pdf.PdfCell.RIGHT;
import static com.atradius.einvoice.ap.pdf.PdfConstants.*;

@Service
public class PdfCreateProcessor implements UblProcessor{
    private PdfMappingData pdfMappingData;
    private UblXmlReader xmlReader;
    private APConfig config;
    private ResourceLoader resourceLoader;

    private PdfCell[] TOTAL_CELLS = new PdfCell[]{
            new PdfCell(new BigDecimal(85), new BigDecimal(40), TWELVE, RIGHT),
            new PdfCell(new BigDecimal(15), new BigDecimal(8.5f), TWELVE, LEFT)
    };

    private PdfCell[] PAYMENT_TABLE_CELLS = new PdfCell[]{
            new PdfCell(FIVE, FIVE , TWELVE, LEFT),
            new PdfCell(new BigDecimal(55), TEN, TWELVE,LEFT),
            new PdfCell(FIVE, TEN, TWELVE, LEFT),
            new PdfCell(new BigDecimal(10), TEN, TWELVE, LEFT),
            new PdfCell(new BigDecimal(10), TEN, TWELVE, LEFT),
            new PdfCell(new BigDecimal(15), TEN, TWELVE, LEFT)
    };

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
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            try(PDPageContentStream contents = new PDPageContentStream(document, page)) {
                PDType0Font regular = PDType0Font.load(document, resourceLoader.getResource("classpath:fonts/NotoSans-Regular.ttf").getInputStream());
                PDType0Font bold = PDType0Font.load(document, resourceLoader.getResource("classpath:fonts/NotoSans-Bold.ttf").getInputStream());
                PdfControls pdfControls = new PdfControls(contents, page.getMediaBox().getWidth(), page.getMediaBox().getHeight(), regular, bold);
                pdfControls.lineNumber = 2;

                String logoPath = resourceLoader.getResource("classpath:logoAtradius_tagline_red.PNG").getFile().getPath();
                PDImageXObject logo = PDImageXObject.createFromFile(logoPath, document);
                pdfControls.addLogo(logo);

                variables.setDocumentType(getDocumentType(data.getUblContent()));
                String rootElement = "INVOICE".equalsIgnoreCase(variables.getDocumentType()) ? "/ns0:Invoice" : "/ns1:CreditNote";
                pdfControls.addHeaderText(variables.getDocumentType());
                pdfControls.lineNumber += 3;

                List<String> supplierData = pdfMappingData.getSupplierData(data.getUblContent(), rootElement);
                List<String> invoiceData = pdfMappingData.getInvoiceData(data.getUblContent(), rootElement);
                addTableData(pdfControls, supplierData, invoiceData);
                pdfControls.lineNumber += 1;

                List<String> customerData = pdfMappingData.getCustomerData(data.getUblContent(), rootElement);
                List<String> bankData = pdfMappingData.getBankData(data.getUblContent(), rootElement);
                addTableData(pdfControls, customerData, bankData);
                //line height difference between table row and normal row, reduce empty space starting table;
                pdfControls.lineNumber += 1;

                List<List<String>> paymentsData = new ArrayList<>();
                paymentsData.add(List.of("ID", "Name", "Qty", "Tax%", "Tax", "Amount"));
                paymentsData.addAll(pdfMappingData.getPaymentsData(data.getUblContent(), rootElement));
                PdfTable paymentTable = new PdfTable(PAYMENT_TABLE_CELLS, paymentsData);
                pdfControls.drawTable(paymentTable);

                List<List<String>> paymentTotalsData = new ArrayList<>();
                String total = xmlReader.getXPathValue(data.getUblContent(), rootElement + config.getTotalAmountPath());
                paymentTotalsData.add(List.of("Tax Inclusive Amount", total));
                PdfTable totalTable = new PdfTable(TOTAL_CELLS, paymentTotalsData);
                pdfControls.drawTable(totalTable);

            }
            document.save(baos);
            data.setPdfContents(baos.toByteArray());
        }
    }

    private void addTableData(PdfControls pdfControls, List<String> left, List<String> right) throws Exception{
        int maxRows = left.size() > right.size() ? left.size() : right.size();
        for(int i = 0; i < maxRows; i++) {
            int leftLines = pdfControls.addText(pdfControls.lineNumber, getListItem(left, i), true);
            int rightLines = pdfControls.addText(pdfControls.lineNumber, getListItem(right, i), false);
            pdfControls.lineNumber += leftLines > rightLines ? leftLines : rightLines;
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

    public static void main(String[] args)throws Exception{
        PdfCreateProcessor p = new PdfCreateProcessor(null, null, null, null);
        p.process(null, null);
    }
}
