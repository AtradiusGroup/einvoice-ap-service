package com.atradius.einvoice.ap.service;

import com.atradius.einvoice.ap.config.APConfig;
import com.atradius.einvoice.ap.model.*;
import com.itextpdf.text.pdf.codec.Base64;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class MailProcessedService {
    private EmailService emailService;
    private TokenService tokenService;
    private APConfig config;
    private LogInfoService logInfoService;
    private JsonConverterService jsonConverter;

    public MailProcessedService(EmailService emailService, TokenService tokenService, APConfig config,
                                LogInfoService logInfoService, JsonConverterService jsonConverter){
        this.emailService = emailService;
        this.tokenService = tokenService;
        this.config = config;
        this.logInfoService = logInfoService;
        this.jsonConverter = jsonConverter;
    }

    public void moveProcessedEmails(){
        LocalDateTime startTime = LocalDateTime.now();
        ResponseEntity<String> msTokenResponse = tokenService.getMSToken(config);
        if (msTokenResponse.getStatusCode().is2xxSuccessful()) {
            String msToken = jsonConverter.getStringValue(msTokenResponse.getBody(), "access_token");
            List<Map> mailMessages = null;
            do {
                ResponseEntity<String> emailListResponse = emailService.getFlaggedMessagesFromProcessed(msToken);
                if (emailListResponse.getStatusCode().is2xxSuccessful()) {
                    mailMessages = jsonConverter.getListValue(emailListResponse.getBody(), "value");
                    for (Map mail : mailMessages) {
                        String messageId = (String) mail.get("id");
                        ResponseEntity<String> messageResponse = emailService.getMessageAttachments(msToken, messageId);
                        if (messageResponse.getStatusCode().is2xxSuccessful()) {
                            Message message = new Message();
                            message.setSubject((String)mail.get("subject"));

                            List<Recipient> recipients = new ArrayList<>();
                            recipients.add(new Recipient(new EmailAddress(config.getRecipientEmailAddress())));
                            message.setToRecipients(recipients);

                            List<Map<String, String>> attachmentList = jsonConverter.getListValue(messageResponse.getBody(), "value");
                            attachmentList.stream().forEach(attachment ->{
                                List<Attachment> attachments = new ArrayList<>();
                                attachments.add(jsonConverter.jsonToObject(jsonConverter.objectToJson(attachment), Attachment.class));
                                message.setAttachments(attachments);
                            });

                            emailService.sendMessage(msToken, new MailMessage(message));

                            ResponseEntity<String> moveResp = emailService.moveMessage(msToken, messageId, config.getArchiveFolder());
                            if (moveResp.getStatusCode().isError()) {
                                logInfoService.logInfo("failed moving to "+ config.getArchiveFolder() + " of subject "+ mail.get("subject"));
                            }
                        }
                    }
                } else {
                    logInfoService.logInfo("Failed to retrieve email list: error status code " +
                            emailListResponse.getStatusCode().value() + " and description " + emailListResponse.getBody());
                }
            }while(mailMessages != null && mailMessages.size() == 10);
        } else {
            logInfoService.logInfo("Failed to retrieve email token: error status code" +
                    msTokenResponse.getStatusCode().value() + "and description " + msTokenResponse.getBody());
        }
        logInfoService.logProcessTime("SendFlaggedEmails", startTime);
    }
}
