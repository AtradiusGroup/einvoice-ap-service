package com.atradius.einvoice.ap.service;

import com.atradius.einvoice.ap.config.APConfig;
import com.atradius.einvoice.ap.model.MailMessage;
import com.atradius.einvoice.ap.model.MoveMessage;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

@Service
public class EmailService{
    private APConfig config;
    private RestService restService;
    private JsonConverterService jsonConverter;
    private LogInfoService logInfoService;

    @Autowired
    public EmailService(APConfig config, JsonConverterService jsonConverter, RestService restService, LogInfoService logInfoService){
        this.restService = restService;
        this.config = config;
        this.jsonConverter = jsonConverter;
        this.logInfoService = logInfoService;
    }

    public ResponseEntity<String> getFlaggedMessagesFromProcessed(String msToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(msToken);
        String url = String.join("",config.getMailUrl(),"/mailFolders/", config.getProcessedFolder(),
                "/messages?$select=id,subject", "&$filter=from/emailAddress/address eq '", config.getMailAddress(),
                "' and flag/flagStatus eq 'Flagged'");
        ResponseEntity<String> responseEntity = null;
        try {
            responseEntity = restService.sendRequest(url, headers, null, HttpMethod.GET);
        }catch (HttpClientErrorException | HttpServerErrorException ee) {
            logInfoService.logError("getMessages request failed", ee);
            responseEntity = ResponseEntity.status(ee.getStatusCode()).body(ee.getResponseBodyAsString());
        }
        return responseEntity;
    }

    public ResponseEntity<String> getMessagesExcludedFrom(String msToken) {
        return getMessages(msToken, config.getMailAddress(), false);
    }

    public ResponseEntity<String> getMessages(String msToken, String from, boolean include) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(msToken);
        String url = String.join("",config.getMailUrl(),"/mailFolders/Inbox/messages?$select=id,subject");
        if(StringUtils.isNotEmpty(from)){
            StringBuilder urlBuilder = new StringBuilder(url);
            urlBuilder.append("&$filter=from/emailAddress/address").append(include ? " eq " : " ne ")
                    .append(" '").append(from).append("'");
            url = urlBuilder.toString();
        }
        ResponseEntity<String> responseEntity = null;
        try {
            responseEntity = restService.sendRequest(url, headers, null, HttpMethod.GET);
        }catch (HttpClientErrorException | HttpServerErrorException ee) {
            logInfoService.logError("getMessages request failed", ee);
            responseEntity = ResponseEntity.status(ee.getStatusCode()).body(ee.getResponseBodyAsString());
        }
        return responseEntity;
    }

    public ResponseEntity<String> getMessage(String authToken, String messageId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authToken);
        headers.set("Prefer", "outlook.body-content-type=\"text\"");
        String url = String.join("",config.getMailUrl(),"/mailFolders/Inbox/messages/", messageId, "?$select=body,subject");
        ResponseEntity<String> responseEntity = null;
        try{
            responseEntity = restService.sendRequest(url, headers, null, HttpMethod.GET);
        }catch (HttpClientErrorException | HttpServerErrorException ee) {
            logInfoService.logError("getMessage request failed", ee);
            responseEntity = ResponseEntity.status(ee.getStatusCode()).body(ee.getResponseBodyAsString());
        }
        return responseEntity;
    }

    public ResponseEntity<String> getMessageAttachments(String authToken, String messageId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authToken);
        headers.set("Prefer", "outlook.body-content-type=\"text\"");
        String url = String.join("",config.getMailUrl(),"/mailFolders/Inbox/messages/", messageId, "/attachments");
        ResponseEntity<String> responseEntity = null;
        try{
            responseEntity = restService.sendRequest(url, headers, null, HttpMethod.GET);
        }catch (HttpClientErrorException | HttpServerErrorException ee) {
            logInfoService.logError("get attachments request failed", ee);
            responseEntity = ResponseEntity.status(ee.getStatusCode()).body(ee.getResponseBodyAsString());
        }
        return responseEntity;
    }

    public ResponseEntity<String> deleteMessage(String authToken, String messageId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authToken);
        ResponseEntity<String> responseEntity = null;
        try{
            responseEntity = restService.sendRequest(config.getMailUrl()+ "/mailFolders/Inbox/messages/"+ messageId, headers,null,
                    HttpMethod.DELETE);
        }catch (HttpClientErrorException | HttpServerErrorException ee) {
            logInfoService.logError("Failed post request ", ee);
            responseEntity = ResponseEntity.status(ee.getStatusCode()).body(ee.getResponseBodyAsString());
        }
        return responseEntity;
    }

    public ResponseEntity<String> moveMessage(String authToken, String messageId, String moveToFolder) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        String url = String.join("/",config.getMailUrl(),"mailFolders/Inbox/messages",messageId,"move");
        ResponseEntity<String> responseEntity = null;
        try{
            responseEntity = restService.sendRequest(url, headers, new MoveMessage(moveToFolder), HttpMethod.POST);
        }catch (HttpClientErrorException | HttpServerErrorException ee) {
            logInfoService.logError("Failed post request ", ee);
            responseEntity = ResponseEntity.status(ee.getStatusCode()).body(ee.getResponseBodyAsString());
        }
        return responseEntity;
    }

    public ResponseEntity<String> sendMessage(String authToken, MailMessage message) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<String> responseEntity = null;
        try{
            String m = jsonConverter.objectToJson(message);
            responseEntity = restService.sendRequest(config.getMailUrl()+ "/sendMail", headers,
                    m, HttpMethod.POST);
        }catch (HttpClientErrorException | HttpServerErrorException ee) {
            logInfoService.logError("Failed post request ", ee);
            responseEntity = ResponseEntity.status(ee.getStatusCode()).body(ee.getResponseBodyAsString());
        }
        return responseEntity;
    }

    public ResponseEntity<String> sendMessageAttachments(String authToken, MailMessage message) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<String> responseEntity = null;
        try{
            String m = jsonConverter.objectToJson(message);
            responseEntity = restService.sendRequest(config.getMailUrl()+ "/sendMail", headers,
                    m, HttpMethod.POST);
        }catch (HttpClientErrorException | HttpServerErrorException ee) {
            logInfoService.logError("Failed post request ", ee);
            responseEntity = ResponseEntity.status(ee.getStatusCode()).body(ee.getResponseBodyAsString());
        }
        return responseEntity;
    }

}