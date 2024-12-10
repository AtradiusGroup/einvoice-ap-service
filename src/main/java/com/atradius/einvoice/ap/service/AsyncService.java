package com.atradius.einvoice.ap.service;

import com.atradius.einvoice.ap.model.EinvoiceVariables;
import com.atradius.einvoice.ap.model.InvoiceData;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import static com.atradius.einvoice.ap.APConstants.WORKFLOW_STAGE_PDFCREATION;
import static com.atradius.einvoice.ap.APConstants.WORKFLOW_STAGE_PDFSENT;

@Service
public class AsyncService {
    protected LogInfoService logInfoService;
    private PdfCreateProcessor pdfCreateProcessor;
    private SendProcessor sendProcessor;

    public AsyncService(LogInfoService logInfoService, PdfCreateProcessor pdfCreateProcessor,
                        SendProcessor sendProcessor){
        this.logInfoService = logInfoService;
        this.pdfCreateProcessor = pdfCreateProcessor;
        this.sendProcessor = sendProcessor;
    }
    @Async("threadPoolTaskExecutor")
    public void startProcess(EinvoiceVariables variables, InvoiceData data)throws Exception{
        logInfoService.logInfo(variables.getCorrelationId(),"current processing stage is "+ variables.getProcessStage());
        if (WORKFLOW_STAGE_PDFCREATION.equals(variables.getProcessStage())) {
            processStage(pdfCreateProcessor, variables, data, WORKFLOW_STAGE_PDFSENT);
        }
        if (WORKFLOW_STAGE_PDFSENT.equals(variables.getProcessStage())) {
            processStage(sendProcessor, variables, data, null);
        }
    }

    private void processStage(UblProcessor processor, EinvoiceVariables variables, InvoiceData data,  String nextStage)throws Exception{
        logInfoService.logInfo(variables.getCorrelationId(), variables.getInvoiceNumber(), "processing invoice content stage");
        variables.setStageStartTime(LocalDateTime.now());
        processor.process(variables, data);
        logInfoService.logInfo(variables.getCorrelationId(), variables.getInvoiceNumber(),"Completed invoice content stage");
        logInfoService.logProcessTime(variables);
        variables.setProcessStage(nextStage);
    }
}
