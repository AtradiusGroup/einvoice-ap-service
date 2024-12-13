package com.atradius.einvoice.ap.pdf;

import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import java.io.IOException;
import java.util.List;

public class PdfControls {
    private float pageWidth;
    private float pageHeight;
    private int fontSzie = 9;
    private int headerFontSize = 12;
    private float lineHeight = 15;
    private float pageMargin = 20;
    private float startX = pageMargin;
    private PDType0Font regular;
    private PDType0Font bold;
    private PDPageContentStream contentStream;

    public PdfControls(PDPageContentStream contentStream, float pageWidth, float pageHeight, PDType0Font regular, PDType0Font bold) {
        this.pageWidth = pageWidth - pageMargin * 2;
        this.pageHeight = pageHeight - pageMargin * 2;
        this.regular = regular;
        this.bold = bold;
        this.contentStream = contentStream;
    }

    public void addHeaderText(int lineNumber, String text) throws IOException {
        contentStream.beginText();
        contentStream.setFont(bold, headerFontSize);
        float fontSize = headerFontSize;
        float textWidth = bold.getStringWidth(text) / 1000 * fontSize;
        float x = (pageWidth - textWidth) / 2;
        float y = getPositionY(lineNumber);
        contentStream.newLineAtOffset(x, y);
        contentStream.showText(text);
        contentStream.endText();
    }

    public void addLogo(int lineNumber, PDImageXObject logo)throws IOException{
        contentStream.drawImage(logo, startX, getPositionY(lineNumber), 150, 40);
    }

    public void addText(int lineNumber, String text, boolean left) throws IOException {
        String[] textParts = text.split(":");
        PDType0Font font = text.indexOf(":") != -1 ? bold : regular;
        String firstPart = text.indexOf(":") != -1 ? textParts[0] + ":" : textParts[0];
        float firstPartLength = (font.getStringWidth(firstPart) / 1000 * fontSzie);
        float secondPartLength = textParts.length == 2 ? (regular.getStringWidth(textParts[1]) / 1000 * fontSzie) : 0;
        contentStream.beginText();
        contentStream.setFont(text.indexOf(":") != -1 ? bold : regular, fontSzie);
        contentStream.newLineAtOffset(left ? startX : (pageWidth - secondPartLength - firstPartLength), getPositionY(lineNumber));
        contentStream.showText(firstPart);
        contentStream.endText();
        if(textParts.length == 2){
            contentStream.beginText();
            contentStream.setFont(regular, fontSzie);
            contentStream.newLineAtOffset(left ? (startX + firstPartLength) : (pageWidth - secondPartLength), getPositionY(lineNumber));
            contentStream.showText(textParts[1]);
            contentStream.endText();
        }
    }

    public void drawTable(PdfTable table, int lineNumber)throws IOException {
        drawTableGrid(table, lineNumber);

        for(int rowIndex = 0; rowIndex < table.rows(); rowIndex++){
            drawRow(table, lineNumber + rowIndex, table.getData().get(rowIndex), rowIndex == 0 ? bold : regular);
        }
    }

    public void drawRow(PdfTable table, int lineNumber, List<String> row, PDType0Font font)throws IOException{
        float xPosition = startX;
        for(int columnIndex = 0; columnIndex < table.columns(); columnIndex++){
            contentStream.beginText();
            contentStream.setFont(font, fontSzie);

            float textWidth = 0;
            float cellMargin = table.getCells()[columnIndex].getMarginX();
            if(PdfCell.RIGHT.equals(table.getCells()[columnIndex].getAlignment())){
                textWidth = font.getStringWidth(row.get(columnIndex)) / 1000 * fontSzie;
                xPosition += table.getCellWidth(pageWidth, columnIndex) - textWidth - cellMargin*2;
            }
            float yPosition = table.cellYPosition(pageHeight, columnIndex, lineNumber);
            contentStream.newLineAtOffset(xPosition + cellMargin, yPosition);
            contentStream.showText(row.get(columnIndex));
            contentStream.endText();
            if(PdfCell.LEFT.equals(table.getCells()[columnIndex].getAlignment())) {
                xPosition += table.getCellWidth(pageWidth, columnIndex);
            }else{
                xPosition += textWidth + cellMargin*2;
            }
        }
    }


    public void drawTableGrid(PdfTable table, int lineNumber)throws IOException {
        //Draw horizantle lines
        for(int rowIndex = 0; rowIndex <= table.rows(); rowIndex++){
            float yPosition = table.yPosition(pageHeight, lineNumber + rowIndex);
            contentStream.moveTo(startX, yPosition);
            contentStream.lineTo(pageWidth, yPosition);
            contentStream.stroke();
        }

        //Draw verticle lines
        float xPosition = startX;
        float yPosition = table.yPosition(pageHeight, lineNumber);
        for(int cellIndex = 0; cellIndex <= table.columns(); cellIndex++){
            contentStream.moveTo(xPosition, yPosition);
            contentStream.lineTo(xPosition, yPosition - table.rows() * table.rowHeight());
            contentStream.stroke();

            //No need to draw verticle line for last column
            if(cellIndex != table.columns()) {
                xPosition += table.getCellWidth(pageWidth, cellIndex);
            }
            if(cellIndex == table.columns() -1){
                xPosition = pageWidth;
            }
        }
    }


    private float getPositionX(String text, boolean left)throws IOException{
        return left ? startX : (pageWidth - (regular.getStringWidth(text) / 1000 * fontSzie));

    }

    private float getPositionY(int lineNumber)throws IOException{
        return pageHeight - lineNumber * lineHeight;

    }
}
