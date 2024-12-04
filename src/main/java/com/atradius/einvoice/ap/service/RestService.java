package com.atradius.einvoice.ap.service;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;

@Service
public class RestService{
    private RestTemplate restTemplate;
    private LogInfoService logInfoService;

    public RestService(LogInfoService logInfoService){
        this.logInfoService = logInfoService;
        this.restTemplate = new RestTemplate();
        this.restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());
    }

    public  <R> ResponseEntity<R> postRequest(String url, Object content, HttpHeaders headers, Class<R> responseType,
                                              String serviceProvider, String messageId, String correlationId){
        logInfoService.logInfo(messageId, correlationId,"Post request started");
        ResponseEntity<R> response = null;
        try {
            HttpEntity requestEntity = new HttpEntity<>(content, headers);
            response = restTemplate.postForEntity(url, requestEntity, responseType);
        }catch (HttpClientErrorException | HttpServerErrorException ee) {
            logInfoService.logError(messageId, correlationId,"Failed post request ", ee);
            response = ResponseEntity.status(ee.getStatusCode()).body(transformError(ee, responseType, serviceProvider));
        }catch (Exception e){
            logInfoService.logError(messageId, correlationId,"Failed process request for "+ url, e);
            response = ResponseEntity.status(HttpStatus.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()))
                    .body(transformError(e, responseType, serviceProvider));
        }
        logInfoService.logInfo("Post request finished");
        return response;
    }
    public <T> ResponseEntity<T> getForEntity(String url, HttpHeaders headers, Class<T> responseType,
                                              String serviceProvider, String messageId, String correlationId){
        logInfoService.logInfo(messageId, correlationId,"Get for entity request started");
        HttpEntity requestEntity = new HttpEntity<>(headers);
        ResponseEntity<T> responseEntity = null;
        try {
            responseEntity = restTemplate.exchange(url, HttpMethod.GET, requestEntity, responseType);
        }catch (HttpClientErrorException | HttpServerErrorException ee) {
            logInfoService.logError(messageId, correlationId,"Failed post request with status "+ ee.getStatusCode(), ee);
            responseEntity = ResponseEntity.status(ee.getStatusCode()).body(transformError(ee, responseType, serviceProvider));
        }catch (Exception e){
            logInfoService.logError(messageId, correlationId,"Failed post request ", e);
            responseEntity = ResponseEntity.status(HttpStatus.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()))
                    .body(transformError(e, responseType, serviceProvider));
        }
        logInfoService.logInfo(messageId, correlationId,"Get for entity request finished");
        return responseEntity;
    }

    public<R> ResponseEntity<R> putForEntity(String url, HttpHeaders headers, Object content, Class<R> responseType,
                                             String serviceProvider, String messageId, String correlationId){
        ResponseEntity<R> response = ResponseEntity.ok().build();
        try {
            restTemplate.put(url, new HttpEntity(content, headers));
        }catch (HttpClientErrorException | HttpServerErrorException ee) {
            logInfoService.logError(messageId, correlationId,"Failed post request ", ee);
            response = ResponseEntity.status(ee.getStatusCode()).body(transformError(ee, responseType, serviceProvider));
        }catch (Exception e){
            logInfoService.logError(messageId, correlationId,"Failed process request for "+ url, e);
            response = ResponseEntity.status(HttpStatus.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()))
                    .body(transformError(e, responseType, serviceProvider));
        }
        return response;
    }

    protected ResponseEntity<String> sendRequest(String url, HttpHeaders headers, Object body, HttpMethod method) {
        HttpEntity requestEntity = new HttpEntity<>(body, headers);
        ResponseEntity<String> responseEntity = null;
        try {
            responseEntity = restTemplate.exchange(url, method, requestEntity, String.class);
        }catch (HttpClientErrorException | HttpServerErrorException ee) {
            logInfoService.logError("Failed post request ", ee);
            responseEntity = ResponseEntity.status(ee.getStatusCode()).body(ee.getResponseBodyAsString());
        }catch (Exception e){
            logInfoService.logError("Failed to send request", e);
            responseEntity = ResponseEntity.status(HttpStatus.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()))
                    .body(e.getMessage());
        }
        return responseEntity;
    }

    private<R> R transformError(Exception e, Class<R> responseType, String serviceProvider){
        R errorBody = null;
        if(responseType.isAssignableFrom(String.class)){
            errorBody = (R)ExceptionUtils.getMessage(e);
        }else if(responseType.isAssignableFrom(byte[].class)){
            errorBody = (R)ExceptionUtils.getMessage(e).getBytes(StandardCharsets.UTF_8);
        }else{
            errorBody = null;
        }
        return errorBody;
    }
}
