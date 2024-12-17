package com.atradius.einvoice.ap.service;

import com.atradius.einvoice.ap.config.APConfig;
import com.atradius.einvoice.ap.model.*;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

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

    public void sendFlaggedEmails(){
        LocalDateTime startTime = LocalDateTime.now();
        ResponseEntity<String> msTokenResponse = tokenService.getMSToken(config);
        if (msTokenResponse.getStatusCode().is2xxSuccessful()) {
            String msToken = jsonConverter.getStringValue(msTokenResponse.getBody(), "access_token");
            List<Map> mailMessages = null;
            do {
                mailMessages = processMessages(msToken, mailMessages);
            }while(mailMessages != null && mailMessages.size() == 10);
        } else {
            logInfoService.logInfo("Failed to retrieve email token: error status code" +
                    msTokenResponse.getStatusCode().value() + "and description " + msTokenResponse.getBody());
        }
        logInfoService.logProcessTime("SendFlaggedEmails", startTime);
    }

    private List<Map> processMessages(String msToken, List<Map> mailMessages) {
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


                    List<Attachment> attachments = new ArrayList<>();
                    List<Map<String, String>> attachmentList = jsonConverter.getListValue(messageResponse.getBody(), "value");
                    attachmentList.stream().forEach(attachment ->{
                        attachments.add(new Attachment((String)attachment.get("contentBytes"),(String)attachment.get("contentType"),
                                (String)attachment.get("name"), (String)attachment.get("@odata.type")));
                        message.setAttachments(attachments);
                    });

                    moveMail(mail, msToken, message, messageId);
                }
            }
        } else {
            logInfoService.logInfo("Failed to retrieve email list: error status code " +
                    emailListResponse.getStatusCode().value() + " and description " + emailListResponse.getBody());
        }
        return mailMessages;
    }

    private void moveMail(Map mail, String msToken, Message message, String messageId) {
        ResponseEntity<String> sendResp = emailService.sendMessage(msToken, new MailMessage(message));
        if(sendResp.getStatusCode().is2xxSuccessful()){
            logInfoService.logInfo("Processed pdf sent "+ config.getRecipientEmailAddress() + " of subject "+ mail.get("subject"));
        }else{
            logInfoService.logInfo("Processed pdf failed sending to "+ config.getRecipientEmailAddress() + " with error "+ sendResp.getBody());
        }

        ResponseEntity<String> moveResp = emailService.moveMessage(msToken, messageId, config.getArchiveFolder());
        if (moveResp.getStatusCode().isError()) {
            logInfoService.logInfo("failed moving to "+ config.getArchiveFolder() + " of subject "+ mail.get("subject"));
        }
    }
}
