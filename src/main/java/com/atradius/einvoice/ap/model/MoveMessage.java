package com.atradius.einvoice.ap.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class MoveMessage {
    @JsonProperty("destinationId")
    private String destinationId;
}
