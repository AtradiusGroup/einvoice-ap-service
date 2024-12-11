package com.atradius.einvoice.ap.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Message {
    @JsonProperty("toRecipients")
    private List<Recipient> toRecipients = new ArrayList<>();
    @JsonProperty("attachments")
    private List<Attachment> attachments = new ArrayList<>();
    @JsonProperty("subject")
    private String subject;
    @JsonProperty("ccRecipients")
    private List<Recipient> ccRecipients = new ArrayList<>();
    @JsonProperty("body")
    private MessageBody body;
    @JsonProperty("hasAttachments")
    private Boolean hasAttachments;
}