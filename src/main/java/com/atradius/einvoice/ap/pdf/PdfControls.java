package com.atradius.einvoice.ap.pdf;

import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import static com.atradius.einvoice.ap.pdf.PdfConstants.*;

public class PdfControls {
    private BigDecimal pageWidth;
    private BigDecimal pageHeight;
    private BigDecimal startX = PAGE_MARGIN;
    private PDType0Font regular;
    private PDType0Font bold;
    private PDPageContentStream contentStream;
    public int lineNumber = 3;

    public PdfControls(PDPageContentStream contentStream, float pageWidth, float pageHeight, PDType0Font regular, PDType0Font bold) {
        this.pageWidth = new BigDecimal(pageWidth).subtract(PAGE_MARGIN.multiply(TWO));
        this.pageHeight = new BigDecimal(pageHeight).subtract(PAGE_MARGIN.multiply(TWO));
        this.regular = regular;
        this.bold = bold;
        this.contentStream = contentStream;
    }

    public void addHeaderText(String text) throws IOException {
        contentStream.beginText();
        contentStream.setFont(bold, HEADER_FONT_SIZE);
        float x = divide(pageWidth.subtract(getTextWidth(text, bold, HEADER_FONT_SIZE)), TWO).floatValue();
        float y = getPositionY(lineNumber).floatValue();
        contentStream.newLineAtOffset(x, y);
        contentStream.showText(text);
        contentStream.endText();
    }

    public void addLogo(PDImageXObject logo)throws IOException{
        contentStream.drawImage(logo, startX.floatValue(), getPositionY(lineNumber).floatValue(), 150, 40);
    }

    public int addText(int lineNumber, String text, boolean left) throws IOException {
        String[] textParts = text.split(":");
        PDType0Font font = text.indexOf(":") != -1 ? bold : regular;
        String firstPart = text.indexOf(":") != -1 ? textParts[0] + ": " : textParts[0];
        BigDecimal middlePosition = divide(pageWidth,new BigDecimal(2));
        List<String> lines = wrapText(firstPart, middlePosition);
        BigDecimal firstPartLength = getTextWidth(lines.get(0), font, FONT_SIZE);
        List<String> lines2 = textParts.length == 2 ? wrapText(textParts[1], middlePosition.subtract(firstPartLength)) : new ArrayList<>();
        BigDecimal secondPartLength = lines2.size() > 0 ? getTextWidth(lines2.get(0), regular, FONT_SIZE) : new BigDecimal(0);

        addText(lines, BigDecimal.ZERO, secondPartLength, left, false);
        if(textParts.length == 2){
            addText(lines2, firstPartLength, secondPartLength, left, true);
        }
        return lines.size() < lines2.size() ? lines2.size() : lines.size();
    }

    private void addText(List<String> lines, BigDecimal firstPartLength, BigDecimal secondPartLength, boolean left, boolean secondPart) throws IOException{
        for(int lineIndex = 0; lineIndex < lines.size(); lineIndex++) {
            contentStream.beginText();
            contentStream.setFont(lines.get(lineIndex).indexOf(":") != -1 ? bold : regular, FONT_SIZE);
            if(left){
                contentStream.newLineAtOffset(lineIndex == 0 ? firstPartLength.add(startX).floatValue() :
                        startX.floatValue(), getPositionY(lineNumber + lineIndex).floatValue());
            }else{
                BigDecimal lineLength = getTextWidth(lines.get(lineIndex),regular, FONT_SIZE);
                BigDecimal x = lineIndex == 0 ? pageWidth.subtract(secondPartLength).subtract(secondPart ? new BigDecimal(0): lineLength): pageWidth;
                contentStream.newLineAtOffset( x.floatValue(), getPositionY(lineNumber + lineIndex).floatValue());
            }

            contentStream.showText(lines.get(lineIndex));
            contentStream.endText();
        }
    }

    public void drawTable(PdfTable table)throws IOException {
        for(int rowIndex = 0; rowIndex < table.rows(); rowIndex++){
            drawHarizantalLine(table);
            int rowsize = drawRow(table, table.getData().get(rowIndex), rowIndex == 0 ? bold : regular, lineNumber);
            drawVerticleLine(table, rowsize);
            lineNumber += rowsize + 1;
        }
        drawHarizantalLine(table);
    }

    public int drawRow(PdfTable table, List<String> row, PDType0Font font, int lineNumber)throws IOException{
        int rowsize = 0;
        BigDecimal xPosition = startX;
        for(int columnIndex = 0; columnIndex < table.columns(); columnIndex++){
            BigDecimal cellWidth = table.getCellWidth(pageWidth, columnIndex);
            List<String> lines = wrapText(row.get(columnIndex), cellWidth);
            rowsize = lines.size() -1 > rowsize ? lines.size()-1 : rowsize;
            BigDecimal cellMargin = table.getCells()[columnIndex].getMarginX();
            BigDecimal textWidth = ZERO;
            if (PdfCell.RIGHT.equals(table.getCells()[columnIndex].getAlignment())) {
                textWidth = getTextWidth(lines.get(0), font, FONT_SIZE -1);
                xPosition = xPosition.add(cellWidth).subtract(textWidth).subtract(cellMargin.multiply(BigDecimal.TWO));
            }

            int cellLineNumber = lineNumber;
            for(int lineIndex = 0; lineIndex < lines.size(); lineIndex++) {
                cellLineNumber += lineIndex;
                BigDecimal yPosition = table.cellYPosition(pageHeight, columnIndex, cellLineNumber);

                contentStream.beginText();
                contentStream.setFont(font, FONT_SIZE-1);
                contentStream.newLineAtOffset(xPosition.add(cellMargin).floatValue(), yPosition.floatValue());
                contentStream.showText(lines.get(lineIndex));
                contentStream.endText();
            }

            if (PdfCell.LEFT.equals(table.getCells()[columnIndex].getAlignment())) {
                xPosition = xPosition.add(cellWidth);
            } else {
                xPosition = xPosition.add(textWidth).add(cellMargin.multiply(BigDecimal.TWO));
            }
        }
        return rowsize;
    }

    private void drawHarizantalLine(PdfTable table)throws IOException{
        BigDecimal yPosition = table.yPosition(pageHeight, lineNumber);
        contentStream.moveTo(startX.floatValue(), yPosition.floatValue());
        contentStream.lineTo(pageWidth.floatValue(), yPosition.floatValue());
        contentStream.stroke();
    }

    private void drawVerticleLine(PdfTable table, int rowsize)throws IOException{
        //Draw verticle lines
        BigDecimal xPosition = startX;
        BigDecimal yPosition = table.yPosition(pageHeight, lineNumber);
        for(int cellIndex = 0; cellIndex <= table.columns(); cellIndex++){
            contentStream.moveTo(xPosition.floatValue(), yPosition.floatValue());
            contentStream.lineTo(xPosition.floatValue(), yPosition.subtract(LINE_HEIGHT).subtract(LINE_HEIGHT.multiply(new BigDecimal(rowsize))).floatValue());
            contentStream.stroke();

            //No need to draw verticle line for last column
            if(cellIndex != table.columns()) {
                xPosition = xPosition.add(table.getCellWidth(pageWidth, cellIndex));
            }
            if(cellIndex == table.columns() -1){
                xPosition = pageWidth;
            }
        }
    }


    private BigDecimal getPositionX(String text, boolean left)throws IOException{
        return left ? startX : pageWidth.subtract(getTextWidth(text, regular, FONT_SIZE));

    }

    private BigDecimal getPositionY(int lineNumber)throws IOException{
        return pageHeight.subtract(new BigDecimal(lineNumber).multiply(LINE_HEIGHT));

    }

    private List<String> wrapText(String text, BigDecimal maxWidth)throws IOException{
        List<String> lines = new ArrayList<>();
        String[] parts = text.split(" ", -1);
        StringBuilder line = new StringBuilder(addSpaceAfterColon(parts[0]));
        for(int index = 1; index < parts.length; index++){
            String lineText = addSpaceAfterColon( parts[index]);
            BigDecimal textWidth = getTextWidth(line.toString()+ " " + lineText, regular, FONT_SIZE);
            if( textWidth.floatValue() <= maxWidth.floatValue()){
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

    private BigDecimal getTextWidth(String text, PDType0Font font, int fontSzie)throws IOException{
        return divide(new BigDecimal(font.getStringWidth(text)), THOUSAND).multiply(new BigDecimal(fontSzie));
    }
    public BigDecimal divide(BigDecimal numerator, BigDecimal denominator){
        return numerator.divide(denominator, 2, RoundingMode.HALF_EVEN);
    }
}
