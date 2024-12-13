package com.atradius.einvoice.ap.pdf;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Data
@AllArgsConstructor
public class PdfTable {
    private PdfCell[] cells;
    private List<List<String>> data;

    public float getCellWidth(float pageWidth, int cellIndex){
        float totalCellMargins = 0;
        for(PdfCell cell : cells){
            totalCellMargins += cell.getMarginX() * 2;
        }
        float tableWidth = pageWidth -  totalCellMargins;
        return (cells[cellIndex].getWidth() * tableWidth * 0.01f) + cells[cellIndex].getMarginX() * 2;
    }

    public int columns(){
        return cells.length;
    }

    public int rows(){
        return data.size();
    }

    public float rowHeight(){
        return 20;
    }

    public float cellYPosition(float pageHeight, int columnIndex, int lineNumber){
        return yPosition(pageHeight, lineNumber) - cells[columnIndex].getMarginY();
    }

    public float yPosition(float pageHeight, int lineNumber){
        return pageHeight - lineNumber * rowHeight();
    }
}
