package com.atradius.einvoice.ap.config;

import com.atradius.einvoice.ap.model.EinvoiceVariables;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

import static com.atradius.einvoice.ap.APConstants.WORKFLOW_STAGE_PDFCREATION;

@Data
@NoArgsConstructor
@Component
public class APConfig {
    @Value("${services.retryCount:0}")
    private int retryCount;
    @Value("${services.retryTimer:1}")
    private int retryTimer;
    @Value("${services.timer:2}")
    private String timer;
    @Value("${services.dms.url}")
    private String dmsUrl;
    @Value("${services.dms.scope}")
    private String dmsScope;
    @Value("${services.dms.basicAuth}")
    private String dmsBasicAuth;
    @Value("${services.dms.iddName}")
    private String dmsIDDName;

    @Value("${services.oauth.basicAuth:}")
    private String basicAuth;
    @Value("${services.oauth.grantType}")
    private String grantType;
    @Value("${services.oauth.scope}")
    private String scope;
    @Value("${services.oauth.tokenUrl}")
    private String tokenUrl;
    @Value("${services.oauth.tokenInfoUrl:}")
    private String tokenInfoUrl;
    @Value("${services.oauth.identityDomainName}")
    private String identityDomainName;

    @Value("${services.mail.url}")
    private String mailUrl;
    @Value("${services.mail.tokenUrl}")
    private String mailTokenUrl;
    @Value("${services.mail.tokenGrantType}")
    private String mailTokenGrantType;
    @Value("${services.mail.tokenScope}")
    private String mailTokenScope;
    @Value("${services.mail.tokenClientId}")
    private String mailTokenClientId;
    @Value("${services.mail.tokenClientSecret}")
    private String mailTokenClientSecret;
    @Value("${services.mail.errorMailAddress}")
    private String errorMailAddress;
    @Value("${services.mail.archiveFolder}")
    private String archiveFolder;
    @Value("${services.mail.from}")
    private String mailFrom;

    @Value("${services.billtrust.url}")
    private String billtrustUrl;
    @Value("${services.billtrust.basicAuthToken}")
    private String basicAuthToken;
    @Value("${services.billtrust.loginPath}")
    private String loginPath;
    @Value("${services.billtrust.senderPath}")
    private String senderPath;
    @Value("${services.billtrust.uploadPath}")
    private String uploadPath;
    @Value("${services.maxProcessedCount}")
    private Integer maxProcessedCount;

    public EinvoiceVariables addVariables(String invoiceNumber, String documentFileType, String supplierParty, String ublContent) {
        EinvoiceVariables variables = new EinvoiceVariables(UUID.randomUUID().toString(), invoiceNumber,
                java.util.Date.from(Instant.now()), WORKFLOW_STAGE_PDFCREATION, timer);
        return variables;
    }
}
