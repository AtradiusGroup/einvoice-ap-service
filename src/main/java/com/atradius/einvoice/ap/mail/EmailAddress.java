package com.atradius.einvoice.bpm.model.mail;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class EmailAddress {
    @JsonProperty("address")
    private String address;
}