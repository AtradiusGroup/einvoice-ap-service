package com.atradius.einvoice.ap.service;

import com.atradius.einvoice.ap.model.EinvoiceVariables;
import com.atradius.einvoice.ap.model.InvoiceData;

public interface UblProcessor {
    void process(EinvoiceVariables variables, InvoiceData data)throws Exception;
}
