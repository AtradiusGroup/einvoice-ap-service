package com.atradius.einvoice.ap.service;

import com.atradius.einvoice.ap.model.EinvoiceVariables;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class LogInfoService {
    @Autowired
    private JsonConverterService converterService;

    public void logInfo(String correlationId,String invoiceNumber, String info){
        Map<String, Object> data = new HashMap<>();
        data.put("correlationId", correlationId);
        data.put("invoiceNumber", invoiceNumber);
        data.put("info", info);
        log.info(converterService.objectToJson(data));
    }

    public void logProcessTime(EinvoiceVariables variables, LocalDateTime startTime){
        Map<String, Object> data = new HashMap<>();
        data.put("correlationId", variables.getCorrelationId());
        data.put("invoiceNumber", variables.getInvoiceNumber());
        data.put("stage", variables.getProcessStage());
        data.put("timeTaken", Duration.between(startTime, LocalDateTime.now()).toMillis());
        log.info(converterService.objectToJson(data));
    }

    public void logProcessTime(EinvoiceVariables variables, String stage, LocalDateTime startTime){
        Map<String, Object> data = new HashMap<>();
        data.put("correlationId", variables.getCorrelationId());
        data.put("invoiceNumber", variables.getInvoiceNumber());
        data.put("stage", stage);
        data.put("timeTaken", Duration.between(startTime, LocalDateTime.now()).toMillis());
        log.info(converterService.objectToJson(data));
    }

    public void logProcessTime(String stage, LocalDateTime startTime){
        Map<String, Object> data = new HashMap<>();
        data.put("stage", stage);
        data.put("timeTaken", Duration.between(startTime, LocalDateTime.now()).toMillis());
        log.info(converterService.objectToJson(data));
    }

    public void logInfo(String correlationId, String info){
        Map<String, Object> data = new HashMap<>();
        data.put("correlationId", correlationId);
        data.put("info", info);
        log.info(converterService.objectToJson(data));
    }

    public void logInfo(String info){
        Map<String, Object> data = new HashMap<>();
        data.put("info", info);
        log.info(converterService.objectToJson(data));
    }

    public void logObject(Object data){
        log.info(converterService.objectToJson(data));
    }

    public void logError(String correlationId, String invoiceNumber, String error, Throwable t){
        Map<String, Object> data = new HashMap<>();
        data.put("correlationId", correlationId);
        data.put("invoiceNumber", invoiceNumber);
        data.put("errorMsg", error);
        data.put("errorDetails", ExceptionUtils.getStackTrace(t));
        log.error(converterService.objectToJson(data));
    }

    public void logError(String correlationId, String error, Throwable t){
        Map<String, Object> data = new HashMap<>();
        data.put("correlationId", correlationId);
        data.put("errorMsg", error);
        data.put("errorDetails", ExceptionUtils.getStackTrace(t));
        log.error(converterService.objectToJson(data));
    }

    public void logError(String error, Throwable t){
        Map<String, Object> data = new HashMap<>();
        data.put("errorMsg", error);
        data.put("errorDetails", ExceptionUtils.getStackTrace(t));
        log.error(converterService.objectToJson(data));
    }
}
