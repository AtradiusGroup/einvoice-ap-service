package com.atradius.einvoice.ap.model;

import lombok.*;

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
}
