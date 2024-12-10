package com.atradius.einvoice.ap.service;

import com.atradius.einvoice.ap.exception.PdfCreateException;
import com.atradius.einvoice.ap.model.EinvoiceVariables;
import com.atradius.einvoice.ap.model.InvoiceData;
import org.springframework.stereotype.Component;

@Component
public class InvoiceContentProcessor implements UblProcessor{
    @Override
    public void process(EinvoiceVariables variables, InvoiceData data) throws PdfCreateException {

    }
}
