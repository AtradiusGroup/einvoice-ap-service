package com.atradius.einvoice.ap.service;

import com.atradius.einvoice.ap.config.ManagerContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ScheduledTasks {
    @Value("${services.leaderElection:}")
    private String localEnv;
    private ManagerContext managerContext;
    private MailRetrieveService mailRetrieveService;
    private MailProcessedService mailProcessedService;

    public ScheduledTasks(ManagerContext managerContext, MailRetrieveService mailRetrieveService,
                          MailProcessedService mailProcessedService){
        this.managerContext = managerContext;
        this.mailRetrieveService = mailRetrieveService;
        this.mailProcessedService = mailProcessedService;
    }
    @Scheduled(cron = "${services.emailSchedule}")
    public void processUblEmailTask() {
        if (isLeader()) {
            mailRetrieveService.processEmails();
        }
    }

    @Scheduled(cron = "${services.emailSchedule}")
    public void processReadyEmailTask() {
        if (isLeader()) {
            mailProcessedService.moveProcessedEmails();
        }
    }

    private boolean isLeader() {
        // Logic to check if this instance is the leader
        return ((managerContext.getContext() != null && managerContext.getContext().isLeader())
                || "Local".equalsIgnoreCase(localEnv));
    }

}
