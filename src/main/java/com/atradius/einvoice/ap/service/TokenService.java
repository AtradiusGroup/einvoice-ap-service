package com.atradius.einvoice.ap.service;

import com.atradius.einvoice.ap.config.APConfig;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import static com.atradius.einvoice.ap.APConstants.*;

@Service
public class TokenService{
    private RestTemplate restTemplate;
    private LogInfoService logInfoService;

    public TokenService(LogInfoService logInfoService){
        this.logInfoService = logInfoService;
        restTemplate = new RestTemplate();
    }

    public ResponseEntity<String> getOIDMToken(String tokenUrl, String grantType, String basicAuth, String scope, String iddName){
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(basicAuth);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add(GRANT_TYPE, grantType);
        body.add(SCOPE, scope);

        HttpEntity requestEntity = new HttpEntity<>(body, headers);
        StringBuilder urlBuilder = new StringBuilder(tokenUrl);
        urlBuilder.append("iddomain=").append(iddName);
        ResponseEntity<String> responseEntity = null;
        try{
            responseEntity = restTemplate.postForEntity(urlBuilder.toString(), requestEntity, String.class);
        }catch (HttpClientErrorException | HttpServerErrorException ee) {
            logInfoService.logError("Failed post request ", ee);
            responseEntity = ResponseEntity.status(ee.getStatusCode()).body(ee.getResponseBodyAsString());
        }
        return responseEntity;
    }

    public ResponseEntity<String> verifyOIDMToken(String url, String token, String iddName){
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-OAUTH-IDENTITY-DOMAIN-NAME", iddName);
        HttpEntity requestEntity = new HttpEntity<>(headers);
        ResponseEntity<String> responseEntity = null;
        try {
            StringBuilder urlBuilder = new StringBuilder(url);
            urlBuilder.append("access_token=").append(token.split("Bearer ")[1].trim());
            responseEntity = restTemplate.exchange(urlBuilder.toString(), HttpMethod.GET, requestEntity, String.class);
        }catch (HttpClientErrorException | HttpServerErrorException ee) {
            logInfoService.logError("Failed post request ", ee);
            responseEntity = ResponseEntity.status(ee.getStatusCode()).body(ee.getResponseBodyAsString());
        }
        return responseEntity;
    }

    public ResponseEntity<String> getMSToken(APConfig config){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add(GRANT_TYPE, config.getMailTokenGrantType());
        body.add(SCOPE, config.getMailTokenScope());
        body.add(CLIENT_ID, config.getMailTokenClientId());
        body.add(CLIENT_SECRET, config.getMailTokenClientSecret());

        HttpEntity requestEntity = new HttpEntity<>(body, headers);
        ResponseEntity<String> responseEntity = null;
        try{
            responseEntity = restTemplate.postForEntity(config.getMailTokenUrl(), requestEntity,
                    String.class);
        }catch (HttpClientErrorException | HttpServerErrorException ee) {
            logInfoService.logError("Failed post request ", ee);
            responseEntity = ResponseEntity.status(ee.getStatusCode()).body(ee.getResponseBodyAsString());
        }
        return responseEntity;
    }
}