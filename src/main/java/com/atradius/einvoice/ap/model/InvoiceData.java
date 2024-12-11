package com.atradius.einvoice.ap.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class InvoiceData {
    private String ublContent;
    private byte[] pdfContents;

}
