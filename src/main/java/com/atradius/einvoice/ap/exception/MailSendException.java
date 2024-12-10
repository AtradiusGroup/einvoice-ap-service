package com.atradius.einvoice.ap.exception;

public class MailSendException extends Exception{
    protected String provider;
    protected String details;
    protected int statusCode;

    public MailSendException(String msg, Throwable t){
        super(msg, t);
    }

    public String getProvider(){
        return provider;
    }
    public String getDetails(){
        return details;
    }
    public int getStatusCode(){return statusCode;}
}
