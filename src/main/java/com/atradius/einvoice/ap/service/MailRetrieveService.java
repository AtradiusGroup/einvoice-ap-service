package com.atradius.einvoice.ap.service;

import com.atradius.einvoice.ap.config.APConfig;
import com.atradius.einvoice.ap.model.EinvoiceVariables;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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
                ResponseEntity<String> emailListResponse = emailService.getMessages(msToken);
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
            ResponseEntity<String> messageResponse = emailService.getMessage(msToken, messageId);
            if (messageResponse.getStatusCode().is2xxSuccessful()) {
                try {
                    Map mailDetails = jsonConverter.getMapValue(messageResponse.getBody(), "body");
                    String mailContent = (String) mailDetails.get("content");
                    mailContent = mailContent.replace(MAIL_WARNING_MSG, "");
                    mailContent = mailContent.substring(0, mailContent.indexOf("</OES_EMAIL_OUT>") + 16);
                    String ublContent = ublXmlReader.retrieveCDATA(mailContent.trim());
                    if (StringUtils.isNotEmpty(ublContent)) {
                        logInfoService.logInfo("Extracted ubl content from email");
                        String invoiceNumber = ublXmlReader.getElementValue(ublContent, "ID", UblXmlReader.CBC_NAMESPACE);
                        String supplierParty = ublXmlReader.getElementValue(ublContent, "AccountingSupplierParty.Party.PartyName.Name", UblXmlReader.CAC_NAMESPACE);
                        String documentFileType = ublXmlReader.getElementValue(ublContent, "Note", null);
                        EinvoiceVariables variables = config.addVariables(invoiceNumber, documentFileType, supplierParty, ublContent);
                        asyncService.startProcess(variables);
                        ResponseEntity<String> moveResp = emailService.moveMessage(msToken, messageId);
                        if (moveResp.getStatusCode().isError()) {
                            logInfoService.logInfo("failed to archive mail subject "+ mail.get("subject"));
                        }
                    } else {
                        logInfoService.logInfo("ubl content is empty");
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