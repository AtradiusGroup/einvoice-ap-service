package com.atradius.einvoice.ap.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class Attachment {
    @JsonProperty("name")
    private String name;
    @JsonProperty("contentType")
    private String contentType;
    @JsonProperty("contentBytes")
    private String contentBytes;
    @JsonProperty("@odata.type")
    private String dataType;
}
