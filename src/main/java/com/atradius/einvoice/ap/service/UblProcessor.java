package com.atradius.einvoice.ap.service;

import com.atradius.einvoice.ap.model.EinvoiceVariables;

public interface UblProcessor {
    void process(EinvoiceVariables variables)throws Exception;
}
