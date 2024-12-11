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
public class MailMoveService {
    private EmailService emailService;
    private TokenService tokenService;
    private APConfig config;
    private LogInfoService logInfoService;
    private JsonConverterService jsonConverter;

    public MailMoveService(EmailService emailService, TokenService tokenService, APConfig config,
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
                ResponseEntity<String> emailListResponse = emailService.getMessagesReceivedFrom(msToken);
                if (emailListResponse.getStatusCode().is2xxSuccessful()) {
                    mailMessages = jsonConverter.getListValue(emailListResponse.getBody(), "value");
                    for (Map mail : mailMessages) {
                        String messageId = (String) mail.get("id");
                        ResponseEntity<String> moveResp = emailService.moveMessage(msToken, messageId, config.getProcessedFolderId());
                        if (moveResp.getStatusCode().isError()) {
                            logInfoService.logInfo("failed moving to "+ config.getProcessedFolder() + " of subject "+ mail.get("subject"));
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
        logInfoService.logProcessTime("MoveProcessedEmails", startTime);
    }
}
