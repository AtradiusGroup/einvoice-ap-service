package com.atradius.einvoice.ap.service;

import com.atradius.einvoice.ap.config.APConfig;
import com.atradius.einvoice.ap.model.EinvoiceVariables;
import com.atradius.einvoice.ap.model.InvoiceData;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Service
public class MailRetrieveService{
    private JsonConverterService jsonConverter;
    private EmailService emailService;
    private TokenService tokenService;
    private UblXmlReader ublXmlReader;
    private APConfig config;
    private AsyncService asyncService;
    private LogInfoService logInfoService;
    private final static String MAIL_WARNING_MSG = "CAUTION: This email originated from outside of Atradius. Do not click links or open attachments unless you recognize the sender and know the content is safe.";

    public MailRetrieveService(JsonConverterService jsonConverter, EmailService emailService, TokenService tokenService,
                               UblXmlReader ublXmlReader, APConfig config, AsyncService asyncService, LogInfoService logInfoService){
        this.jsonConverter = jsonConverter;
        this.emailService = emailService;
        this.tokenService = tokenService;
        this.ublXmlReader = ublXmlReader;
        this.config = config;
        this.asyncService = asyncService;
        this.logInfoService = logInfoService;
    }
    public void processEmails() {
        LocalDateTime startTime = LocalDateTime.now();
        ResponseEntity<String> msTokenResponse = tokenService.getMSToken(config);
        if (msTokenResponse.getStatusCode().is2xxSuccessful()) {
            String msToken = jsonConverter.getStringValue(msTokenResponse.getBody(), "access_token");
            List<Map> mailMessages = null;
            do {
                ResponseEntity<String> emailListResponse = emailService.getMessagesExcludedFrom(msToken);
                if (emailListResponse.getStatusCode().is2xxSuccessful()) {
                    mailMessages = jsonConverter.getListValue(emailListResponse.getBody(), "value");
                    processUBL(mailMessages, msToken);
                } else {
                    logInfoService.logInfo("Failed to retrieve email list: error status code " +
                            emailListResponse.getStatusCode().value() + " and description " + emailListResponse.getBody());
                }
            }while(mailMessages != null && mailMessages.size() == 10);
        } else {
            logInfoService.logInfo("Failed to retrieve email token: error status code" +
                    msTokenResponse.getStatusCode().value() + "and description " + msTokenResponse.getBody());
        }
        logInfoService.logProcessTime("ProcessEmails", startTime);
    }

    private void processUBL(List<Map> mailMessages, String msToken){
        for (Map mail : mailMessages) {
            String messageId = (String) mail.get("id");
            ResponseEntity<String> messageResponse = emailService.getMessageAttachments(msToken, messageId);
            if (messageResponse.getStatusCode().is2xxSuccessful()) {
                try {
                    List<Map<String, String>> attachments = jsonConverter.getListValue(messageResponse.getBody(), "value");
                    String contents = attachments.stream().filter(attachment -> "application/xml".equals(attachment.get("contentType")))
                            .map(attachment -> attachment.get("contentBytes")).findFirst().orElse(null);
                    if (StringUtils.isNotEmpty(contents)) {
                        logInfoService.logInfo("Extracted ubl content from email");
                        EinvoiceVariables variables = config.addVariables();
                        InvoiceData data = new InvoiceData(new String(Base64.getDecoder().decode(contents), "UTF-8"), null);
                        String ublXml = ublXmlReader.getElementValue(data.getUblContent(), "cbc:UBLVersionID");
                        String processedFolder = StringUtils.isNotEmpty(ublXml) ? config.getArchiveFolder() : config.getReviewFolder();
                        if(StringUtils.isNotEmpty(ublXml)) {
                            String invoiceNumber = ublXmlReader.getElementValue(data.getUblContent(), "cbc:ID");
                            logInfoService.logInfo("Processing ubl xml contents received through mail attachment");
                            variables.setInvoiceNumber(invoiceNumber);
                            variables.setMessageSubject((String)mail.get("subject"));
                            asyncService.startProcess(variables, data);
                        }else{
                            logInfoService.logInfo("Unexpected invoice xml content so it will be moved to review folder");
                        }
                        ResponseEntity<String> moveResp = emailService.moveMessage(msToken, messageId, processedFolder);
                        if (moveResp.getStatusCode().isError()) {
                            logInfoService.logInfo("failed moving to "+ processedFolder + " of subject "+ mail.get("subject"));
                        }
                    } else {
                        logInfoService.logInfo("doesn't find the attachment");
                    }
                }catch(Exception ex1){
                    logInfoService.logError("Failed to process email ", ex1);
                }
            } else{
                logInfoService.logInfo("Failed to retrieve email message: error status code " +
                        messageResponse.getStatusCode().value() + " and description " + messageResponse.getBody());
            }
        }
    }
}