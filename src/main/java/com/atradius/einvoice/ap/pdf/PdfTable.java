package com.atradius.einvoice.ap.pdf;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

import static com.atradius.einvoice.ap.pdf.PdfConstants.*;

@Data
@AllArgsConstructor
public class PdfTable {
    private PdfCell[] cells;
    private List<List<String>> data;

    public BigDecimal getCellWidth(BigDecimal pageWidth, int cellIndex){
        BigDecimal totalCellMargins = ZERO;
        for(PdfCell cell : cells){
            totalCellMargins = totalCellMargins.add(cell.getMarginX().multiply( TWO));
        }
        BigDecimal tableWidth = pageWidth.subtract(totalCellMargins);
        return cells[cellIndex].getWidth().multiply(tableWidth)
                .multiply(BigDecimal.valueOf(0.01)).add(cells[cellIndex].getMarginX().multiply(TWO));
    }

    public int columns(){
        return cells.length;
    }

    public int rows(){
        return data.size();
    }

    public BigDecimal cellYPosition(BigDecimal pageHeight, int columnIndex, int lineNumber){
        return yPosition(pageHeight, lineNumber).subtract(cells[columnIndex].getMarginY());
    }

    public BigDecimal yPosition(BigDecimal pageHeight, int lineNumber){
        return pageHeight.subtract(BigDecimal.valueOf(lineNumber).multiply(LINE_HEIGHT));
    }
}
