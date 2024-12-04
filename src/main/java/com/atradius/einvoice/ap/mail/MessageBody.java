package com.atradius.einvoice.bpm.model.mail;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class MessageBody {
    @JsonProperty("contentType")
    private String contentType;
    @JsonProperty("content")
    private String content;
}