package com.atradius.einvoice.ap.pdf;

import static com.atradius.einvoice.ap.pdf.PdfCell.LEFT;
import static com.atradius.einvoice.ap.pdf.PdfCell.RIGHT;

public class PdfTableCellData {
    public static final PdfCell[] paymentTableCells = new PdfCell[]{
            new PdfCell(5, 5, 12, LEFT),
            new PdfCell(55, 5, 12,LEFT),
            new PdfCell(5, 5, 12, LEFT),
            new PdfCell(10, 5, 12, LEFT),
            new PdfCell(10, 5, 12, LEFT),
            new PdfCell(15, 5, 12, LEFT)
    };

    public static final PdfCell[] totalCells = new PdfCell[]{
            new PdfCell(85, 40, 12, RIGHT),
            new PdfCell(15, 8.5f, 12, LEFT)
    };
}
