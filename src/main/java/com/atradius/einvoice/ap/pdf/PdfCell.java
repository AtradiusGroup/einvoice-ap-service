package com.atradius.einvoice.ap.pdf;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class PdfCell {
    public static final String LEFT = "Left";
    public static final String RIGHT = "Right";
    private BigDecimal width;
    private BigDecimal marginX;
    private BigDecimal marginY;
    private String alignment;
}
