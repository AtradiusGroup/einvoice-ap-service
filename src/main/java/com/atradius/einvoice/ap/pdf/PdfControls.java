package com.atradius.einvoice.ap.pdf;

import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PdfControls {
    private float pageWidth;
    private float pageHeight;
    private int fontSzie = 10;
    private int headerFontSize = 12;
    private float lineHeight = 18;
    private float pageMargin = 20;
    private float startX = pageMargin;
    private PDType0Font regular;
    private PDType0Font bold;
    private PDPageContentStream contentStream;
    public int lineNumber = 3;

    public PdfControls(PDPageContentStream contentStream, float pageWidth, float pageHeight, PDType0Font regular, PDType0Font bold) {
        this.pageWidth = pageWidth - pageMargin * 2;
        this.pageHeight = pageHeight - pageMargin * 2;
        this.regular = regular;
        this.bold = bold;
        this.contentStream = contentStream;
    }

    public void addHeaderText(String text) throws IOException {
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

    public void addLogo(PDImageXObject logo)throws IOException{
        contentStream.drawImage(logo, startX, getPositionY(lineNumber), 150, 40);
    }

    public int addText(int lineNumber, String text, boolean left) throws IOException {
        String[] textParts = text.split(":");
        PDType0Font font = text.indexOf(":") != -1 ? bold : regular;
        String firstPart = text.indexOf(":") != -1 ? textParts[0] + ": " : textParts[0];
        List<String> lines = wrapText(firstPart, (pageWidth)/2);
        float firstPartLength = (font.getStringWidth(lines.get(0)) / 1000 * fontSzie);
        List<String> lines2 = textParts.length == 2 ? wrapText(textParts[1], (pageWidth/2)-firstPartLength) : new ArrayList<>();
        float secondPartLength = lines2.size() > 0 ? (regular.getStringWidth(lines2.get(0)) / 1000 * fontSzie) : 0;

        addText(lines, 0, secondPartLength, left, false);
        if(textParts.length == 2){
            addText(lines2, firstPartLength, secondPartLength, left, true);
        }
        return lines.size() < lines2.size() ? lines2.size() : lines.size();
    }

    private void addText(List<String> lines, float firstPartLength, float secondPartLength, boolean left, boolean secondPart) throws IOException{
        for(int lineIndex = 0; lineIndex < lines.size(); lineIndex++) {
            contentStream.beginText();
            contentStream.setFont(lines.get(lineIndex).indexOf(":") != -1 ? bold : regular, fontSzie);
            if(left){
                contentStream.newLineAtOffset(lineIndex == 0 ? (startX + firstPartLength) : startX, getPositionY(lineNumber + lineIndex));
            }else{
                float lineLength = (regular.getStringWidth(lines.get(lineIndex)) / 1000 * fontSzie);
                contentStream.newLineAtOffset(lineIndex == 0 ? (pageWidth - secondPartLength - (secondPart ? 0: lineLength) ): pageWidth, getPositionY(lineNumber + lineIndex));
            }

            contentStream.showText(lines.get(lineIndex));
            contentStream.endText();
        }
    }

    public void drawTable(PdfTable table)throws IOException {
        for(int rowIndex = 0; rowIndex < table.rows(); rowIndex++){
            lineNumber += rowIndex;
            drawHarizantalLine(table);
            int rowsize = drawRow(table, table.getData().get(rowIndex), rowIndex == 0 ? bold : regular, lineNumber);
            drawVerticleLine(table, rowsize);
            lineNumber += rowsize;
        }
        lineNumber += 1;
        drawHarizantalLine(table);
    }

    public int drawRow(PdfTable table, List<String> row, PDType0Font font, int lineNumber)throws IOException{
        int rowsize = 0;
        float xPosition = startX;
        for(int columnIndex = 0; columnIndex < table.columns(); columnIndex++){
            float cellWidth = table.getCellWidth(pageWidth, columnIndex);
            List<String> lines = wrapText(row.get(columnIndex), cellWidth);
            rowsize = lines.size() -1 > rowsize ? lines.size()-1 : rowsize;
            float cellMargin = table.getCells()[columnIndex].getMarginX();
            float textWidth = 0;
            if (PdfCell.RIGHT.equals(table.getCells()[columnIndex].getAlignment())) {
                textWidth = font.getStringWidth(lines.get(0)) / 1000 * (fontSzie-1);
                xPosition += cellWidth - textWidth - cellMargin * 2;
            }

            int cellLineNumber = lineNumber;
            for(int lineIndex = 0; lineIndex < lines.size(); lineIndex++) {
                cellLineNumber += lineIndex;
                float yPosition = table.cellYPosition(pageHeight, columnIndex, cellLineNumber);

                contentStream.beginText();
                contentStream.setFont(font, fontSzie-1);
                contentStream.newLineAtOffset(xPosition + cellMargin, yPosition);
                contentStream.showText(lines.get(lineIndex));
                contentStream.endText();
            }

            if (PdfCell.LEFT.equals(table.getCells()[columnIndex].getAlignment())) {
                xPosition += cellWidth;
            } else {
                xPosition += textWidth + cellMargin * 2;
            }
        }
        return rowsize;
    }

    private void drawHarizantalLine(PdfTable table)throws IOException{
        float yPosition = table.yPosition(pageHeight, lineNumber);
        contentStream.moveTo(startX, yPosition);
        contentStream.lineTo(pageWidth, yPosition);
        contentStream.stroke();
    }

    private void drawVerticleLine(PdfTable table, int rowsize)throws IOException{
        //Draw verticle lines
        float xPosition = startX;
        float yPosition = table.yPosition(pageHeight, lineNumber);
        for(int cellIndex = 0; cellIndex <= table.columns(); cellIndex++){
            contentStream.moveTo(xPosition, yPosition);
            contentStream.lineTo(xPosition, yPosition - table.rowHeight() - table.rowHeight() * rowsize);
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

    private List<String> wrapText(String text, float maxWidth)throws IOException{
        List<String> lines = new ArrayList<>();
        String[] parts = text.split(" ", -1);
        StringBuilder line = new StringBuilder(addSpaceAfterColon(parts[0]));
        for(int index = 1; index < parts.length; index++){
            String lineText = addSpaceAfterColon( parts[index]);
            float textWidth = regular.getStringWidth(line.toString()+ " " + lineText) / 1000 * fontSzie;
            if( textWidth <= maxWidth){
                line.append(" ").append(lineText);
            }else{
                lines.add(line.toString());
                line = new StringBuilder(lineText);
            }
        }
        lines.add(line.toString());
        return lines;
    }

    private String addSpaceAfterColon(String text){
        return text.indexOf(":") != -1 ? text + " ": text;
    }
}
