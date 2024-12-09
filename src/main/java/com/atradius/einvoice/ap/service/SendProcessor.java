package com.atradius.einvoice.ap.service;

import com.atradius.einvoice.ap.config.APConfig;
import com.atradius.einvoice.ap.exception.MailSendException;
import com.atradius.einvoice.ap.exception.PdfCreateException;
import com.atradius.einvoice.ap.model.*;
import com.itextpdf.text.pdf.codec.Base64;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Component
public class SendProcessor implements UblProcessor{
    private EmailService emailService;
    private TokenService tokenService;
    private JsonConverterService jsonConverter;
    private LogInfoService logInfoService;
    private APConfig config;
    public SendProcessor(EmailService emailService, TokenService tokenService, JsonConverterService jsonConverter,
                         LogInfoService logInfoService, APConfig config){
        this.emailService = emailService;
        this.tokenService = tokenService;
        this.jsonConverter = jsonConverter;
        this.logInfoService = logInfoService;
        this.config = config;
    }
    @Override
    @Retryable(retryFor = Exception.class, maxAttemptsExpression = "${services.retryMaxAttempts}",
            backoff = @Backoff(delayExpression = "${services.retryInvoiceTimer}"))
    public void process(EinvoiceVariables variables, InvoiceData data) throws MailSendException {
        try {
            ResponseEntity<String> msTokenResponse = tokenService.getMSToken(config);
            if (msTokenResponse.getStatusCode().is2xxSuccessful()) {
                String msToken = jsonConverter.getStringValue(msTokenResponse.getBody(), "access_token");
                Message message = new Message();
                message.setSubject("Processed " + variables.getMessageSubject());

                List<Recipient> recipients = new ArrayList<>();
                recipients.add(new Recipient(new EmailAddress(config.getMailAddress())));
                message.setToRecipients(recipients);

                List<Attachment> attachments = new ArrayList<>();
                attachments.add(new Attachment(variables.getInvoiceNumber() + ".pdf", "application/pdf",
                        Base64.encodeBytes(data.getPdfContents()), "#microsoft.graph.fileAttachment"));
                attachments.add(new Attachment(variables.getInvoiceNumber() + ".xml", "application/xml",
                        Base64.encodeBytes(data.getUblContent().getBytes(StandardCharsets.UTF_8)), "#microsoft.graph.fileAttachment"));
                message.setAttachments(attachments);
                message.setHasAttachments(true);

                ResponseEntity<String> result = emailService.sendMessage(msToken, new MailMessage(message));
                if(!result.getStatusCode().is2xxSuccessful()){
                    logInfoService.logInfo("Failed to send pdf attachment "+ result.getBody());
                }
            }
        }catch (Exception e){
            throw new MailSendException("Failed to send mail attachments", e);
        }
    }
}
