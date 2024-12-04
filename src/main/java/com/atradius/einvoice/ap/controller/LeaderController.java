package com.atradius.einvoice.ap.controller;

import com.atradius.einvoice.ap.config.ManagerContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.http.ResponseEntity;
import org.springframework.integration.leader.Context;
import org.springframework.integration.leader.event.OnGrantedEvent;
import org.springframework.integration.leader.event.OnRevokedEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.InetAddress;
import java.net.UnknownHostException;

@RestController
@RequestMapping("/leader")
@Slf4j
public class LeaderController {
    private final String host;
    private Context context;
    private ManagerContext managerContext;

    public LeaderController(ManagerContext managerContext) throws UnknownHostException {
        this.host = InetAddress.getLocalHost().getHostName();
        this.managerContext = managerContext;
    }

    /**
     * PUT request to try and revoke a leadership of this instance. If the instance is not
     * a leader, leadership cannot be revoked. Thus "HTTP Bad Request" response. If the
     * instance is a leader, it must have a leadership context instance which can be used
     * to give up the leadership.
     * @return info about leadership
     */
    @PutMapping
    public ResponseEntity<String> revokeLeadership() {
        if (this.context == null) {
            String message = String.format("Cannot revoke leadership because '%s' is not a leader", this.host);
            return ResponseEntity.badRequest().body(message);
        }
        this.context.yield();
        String message = String.format("Leadership revoked for '%s'", this.host);
        return ResponseEntity.ok(message);
    }

    /**
     * Handle a notification that this instance has become a leader.
     * @param event on granted event
     */
    @EventListener
    public void handleEvent(OnGrantedEvent event) {
        System.out.println(String.format("'%s' leadership granted", event.getRole()));
        this.context = event.getContext();
        managerContext.setContext(this.context);
    }

    /**
     * Handle a notification that this instance's leadership has been revoked.
     * @param event on revoked event
     */
    @EventListener
    public void handleEvent(OnRevokedEvent event) {
        System.out.println(String.format("'%s' leadership revoked", event.getRole()));
        this.context = null;
        managerContext.setContext(null);
    }

}
