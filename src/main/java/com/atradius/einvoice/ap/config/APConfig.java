package com.atradius.einvoice.ap.config;

import com.atradius.einvoice.ap.model.EinvoiceVariables;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.atradius.einvoice.ap.APConstants.SERVICE_TYPE;
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
    @Value("${services.mail.archiveFolder}")
    private String archiveFolder;
    @Value("${services.mail.reviewFolder}")
    private String reviewFolder;
    @Value("${services.mail.processedFolder}")
    private String processedFolder;
    @Value("${services.mail.processedFolderId}")
    private String processedFolderId;
    @Value("${services.mail.mailAddress}")
    private String mailAddress;
    @Value("${services.mail.recipientEmailAddress}")
    private String recipientEmailAddress;

    @Value(("${invoice.mapping.supplier}"))
    private List<String> supplierMappings;
    @Value(("${invoice.mapping.invoice}"))
    private List<String> invoiceMappings;
    @Value(("${invoice.mapping.customer}"))
    private List<String> customerMappings;
    @Value(("${invoice.mapping.bank}"))
    private List<String> bankMappings;
    @Value(("${invoice.mapping.payments.mappings}"))
    private List<String> paymentMappings;
    @Value(("${invoice.mapping.payments.totalAmountPath}"))
    private String totalAmountPath;
    @Value(("#{${namespaces}}"))
    private Map<String, String> namespaces;

    public EinvoiceVariables addVariables() {
        EinvoiceVariables variables = new EinvoiceVariables(UUID.randomUUID().toString(), null,
                java.util.Date.from(Instant.now()), WORKFLOW_STAGE_PDFCREATION, timer, SERVICE_TYPE, null,
                null, null);
        return variables;
    }
}
