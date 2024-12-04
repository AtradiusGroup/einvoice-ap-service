package com.atradius.einvoice.bpm.model.mail;

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
    @JsonProperty("subject")
    private String subject;
    @JsonProperty("body")
    private MessageBody body;
    @JsonProperty("toRecipients")
    private List<Recipient> toRecipients = new ArrayList<>();
    @JsonProperty("ccRecipients")
    private List<Recipient> ccRecipients = new ArrayList<>();
}