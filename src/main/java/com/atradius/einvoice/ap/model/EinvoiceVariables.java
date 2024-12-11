package com.atradius.einvoice.ap.model;

import lombok.*;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
public class EinvoiceVariables {
    private String correlationId;
    private String invoiceNumber;
    private Date createdDate;
    private String processStage;
    private String timer;
    private String serviceType;
    private LocalDateTime stageStartTime;
    private String messageSubject;
    private String documentType;
}
