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


    public static final PdfCell[] PAYMENT_TABLE_CELLS = new PdfCell[]{
            new PdfCell(FIVE, FIVE , TWELVE, LEFT),
            new PdfCell(new BigDecimal(55), TEN, TWELVE,LEFT),
            new PdfCell(FIVE, TEN, TWELVE, LEFT),
            new PdfCell(new BigDecimal(10), TEN, TWELVE, LEFT),
            new PdfCell(new BigDecimal(10), TEN, TWELVE, LEFT),
            new PdfCell(new BigDecimal(15), TEN, TWELVE, LEFT)
    };

    public static final PdfCell[] TOTAL_CELLS = new PdfCell[]{
            new PdfCell(new BigDecimal(85), new BigDecimal(40), TWELVE, RIGHT),
            new PdfCell(new BigDecimal(15), new BigDecimal(8.5f), TWELVE, LEFT)
    };
}
