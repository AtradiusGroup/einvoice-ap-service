package com.atradius.einvoice.ap.pdf;

import java.math.BigDecimal;

import static com.atradius.einvoice.ap.pdf.PdfCell.LEFT;
import static com.atradius.einvoice.ap.pdf.PdfCell.RIGHT;

public class PdfConstants {
    public static final BigDecimal TWO = new BigDecimal(2);
    public static final BigDecimal ZERO = new BigDecimal(0);
    public static final BigDecimal FIVE = new BigDecimal(5);
    public static final BigDecimal TEN = new BigDecimal(10);
    public static final BigDecimal TWELVE = new BigDecimal(13);
    public static final BigDecimal THOUSAND = new BigDecimal(1000);
    public static final  BigDecimal LINE_HEIGHT = new BigDecimal(18);
    public static final  BigDecimal PAGE_MARGIN = new BigDecimal(20);
    public static final  int FONT_SIZE = 10;
    public static final  int HEADER_FONT_SIZE = 12;
}
