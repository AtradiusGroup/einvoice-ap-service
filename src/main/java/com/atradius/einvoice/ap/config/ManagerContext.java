package com.atradius.einvoice.ap.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.integration.leader.Context;
import org.springframework.stereotype.Service;


@Service
public class ManagerContext {
    @Value("${email.protocol:#{null}}")
    private Context context;

    public ManagerContext(){
        this.context = null;
    }


    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }
}
