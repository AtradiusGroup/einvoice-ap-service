package com.atradius.einvoice.ap.service;

import com.atradius.einvoice.ap.model.EinvoiceVariables;
import com.atradius.einvoice.ap.exception.PdfCreateException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import static com.atradius.einvoice.ap.APConstants.*;

@Service
public class AsyncService {
    protected LogInfoService logInfoService;
    private PdfCreateProcessor pdfCreateProcessor;

    public AsyncService(LogInfoService logInfoService, PdfCreateProcessor pdfCreateProcessor){
        this.logInfoService = logInfoService;
        this.pdfCreateProcessor = pdfCreateProcessor;
    }
    @Async("threadPoolTaskExecutor")
    public void startProcess(EinvoiceVariables variables)throws Exception{
        logInfoService.logInfo(variables.getCorrelationId(),"current processing stage is "+ variables.getProcessStage());
        LocalDateTime startTime = LocalDateTime.now();
        try {
            if (WORKFLOW_STAGE_PDFCREATION.equals(variables.getProcessStage())) {
                logInfoService.logInfo(variables.getCorrelationId(), variables.getInvoiceNumber(), "processing pdf create stage");
                pdfCreateProcessor.process(variables);
                logInfoService.logInfo(variables.getCorrelationId(), variables.getInvoiceNumber(),"Completed pdf create stage");
                logInfoService.logProcessTime(variables, startTime);
            }
        }catch (PdfCreateException pe){
            logInfoService.logProcessTime(variables, startTime);
        }catch (Exception e){
            logInfoService.logProcessTime(variables, startTime);
        }
    }
}
