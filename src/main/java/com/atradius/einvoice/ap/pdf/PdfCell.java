package com.atradius.einvoice.ap.pdf;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PdfCell {
    public static final String LEFT = "Left";
    public static final String RIGHT = "Right";
    private float width;
    private float marginX;
    private float marginY;
    private String alignment;
}
