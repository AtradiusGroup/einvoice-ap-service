package com.atradius.einvoice.ap.service;

import com.atradius.einvoice.ap.model.EinvoiceVariables;
import com.atradius.einvoice.ap.exception.PdfCreateException;
import org.springframework.stereotype.Component;

@Component
public class PdfCreateProcessor implements UblProcessor{
    @Override
    public void process(EinvoiceVariables variables) throws PdfCreateException {

    }
}
