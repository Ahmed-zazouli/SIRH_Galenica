package com.moovapps.EVALUATION.Helpers;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellUtil;

import java.util.ArrayList;
import java.util.List;

public class ExcelHelper {

    public static Cell mergeCellsAndSetValue(Sheet sheet, int firstRow, int lastRow, int firstCol, int lastCol, String value) {
        // Merge the cells
        CellRangeAddress mergedRegion = new CellRangeAddress(firstRow, lastRow, firstCol, lastCol);
        sheet.addMergedRegion(mergedRegion);

        // Create a CellStyle for centering both horizontally and vertically
        Workbook workbook = sheet.getWorkbook();
        CellStyle centerStyle = workbook.createCellStyle();
        centerStyle.setAlignment(HorizontalAlignment.CENTER);
        centerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        centerStyle.setWrapText(true); // Enable text wrapping

        // Set the value for the center cell
        Row mergedRow = sheet.getRow(firstRow);
        if (mergedRow == null) {
            mergedRow = sheet.createRow(firstRow);
        }
        Cell mergedCell = mergedRow.createCell(firstCol);
        mergedCell.setCellValue(value);
        mergedCell.setCellStyle(centerStyle);

        return mergedCell;
    }

    public static List<Cell> mergeCellsAndSetValues(Sheet sheet, int firstRow, int lastRow, int firstCol, int lastCol, String value) {
        List<Cell> mergedCells = new ArrayList<>();

        // Merge the cells
        CellRangeAddress mergedRegion = new CellRangeAddress(firstRow, lastRow, firstCol, lastCol); // Replace with your desired merged cell range
        sheet.addMergedRegion(mergedRegion);
        //sheet.addMergedRegion();
        for (int i = mergedRegion.getFirstColumn(); i <= mergedRegion.getLastColumn(); i++) {
            sheet.setColumnWidth(i, Math.max(sheet.getColumnWidth(i), 10 * 400)); // Set the minimum width (e.g., 10 characters wide)

        }
        Workbook workbook = sheet.getWorkbook();
        CellStyle centerStyle = workbook.createCellStyle();
        centerStyle.setAlignment(HorizontalAlignment.CENTER);
        centerStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        // Iterate through the merged region and set the value for each cell
        for (int rowIndex = firstRow; rowIndex <= lastRow; rowIndex++) {
            Row row = sheet.getRow(rowIndex);
            if (row == null) {
                row = sheet.createRow(rowIndex);
            }

            for (int colIndex = firstCol; colIndex <= lastCol; colIndex++) {
                Cell cell = row.getCell(colIndex);
                if (cell == null) {
                    cell = row.createCell(colIndex);
                }


                cell.setCellStyle(centerStyle);
                // Set the value for the cell
                cell.setCellValue(value);
                // Add the cell to the list of merged cells
                mergedCells.add(cell);
            }
        }

        return mergedCells;
    }

    public static Cell align(Cell cell,HorizontalAlignment h,VerticalAlignment v){
        CellUtil.setAlignment(cell, h);
        CellUtil.setVerticalAlignment(cell, v);
        return cell;
    }

    public static Cell addBorder(Workbook workbook, Cell cell) {
        CellStyle borderStyle = workbook.createCellStyle();
        borderStyle.setBorderTop(BorderStyle.MEDIUM);
        borderStyle.setBorderBottom(BorderStyle.MEDIUM);
        borderStyle.setBorderLeft(BorderStyle.MEDIUM);
        borderStyle.setBorderRight(BorderStyle.MEDIUM);
        cell.setCellStyle(borderStyle);
        return cell;
    }

    public static Cell createCell(Sheet sheet , int rowIndex , int columnIndex ) {
        Cell cell = null;
        Row row = sheet.getRow(rowIndex);
        if(row!=null){
             cell = row.getCell(columnIndex);
             if(cell==null){
                 cell = row.createCell(columnIndex);
             }
        }else{
             row = sheet.createRow(rowIndex);
            cell = row.getCell(columnIndex);
            if(cell==null){
                cell = row.createCell(columnIndex);
            }
        }
        sheet.autoSizeColumn(columnIndex);
        return cell;
    }

    public static Cell removeBorder(Workbook workbook, Cell cell) {
        CellStyle borderStyle = cell.getCellStyle();
        borderStyle.setBorderTop(BorderStyle.NONE);
        borderStyle.setBorderBottom(BorderStyle.NONE);
        borderStyle.setBorderLeft(BorderStyle.NONE);
        borderStyle.setBorderRight(BorderStyle.NONE);
        cell.setCellStyle(borderStyle);
        return cell;
    }

    public static Cell addLeftBorder(Workbook workbook, Cell cell) {
        CellStyle borderStyle = cell.getCellStyle();
        borderStyle.setBorderTop(BorderStyle.NONE);
        borderStyle.setBorderBottom(BorderStyle.NONE);
        borderStyle.setBorderLeft(BorderStyle.THIN);
        borderStyle.setBorderRight(BorderStyle.NONE);
        cell.setCellStyle(borderStyle);
        return cell;
    }

    public static Cell addRightBorder(Workbook workbook, Cell cell) {
        CellStyle borderStyle = cell.getCellStyle();
        borderStyle.setBorderTop(BorderStyle.NONE);
        borderStyle.setBorderBottom(BorderStyle.NONE);
        borderStyle.setBorderLeft(BorderStyle.NONE);
        borderStyle.setBorderRight(BorderStyle.THIN);
        cell.setCellStyle(borderStyle);
        return cell;
    }

    public static Cell addTopBorder(Workbook workbook, Cell cell) {
        CellStyle borderStyle = cell.getCellStyle();
        borderStyle.setBorderTop(BorderStyle.THIN);
        borderStyle.setBorderBottom(BorderStyle.NONE);
        borderStyle.setBorderLeft(BorderStyle.NONE);
        borderStyle.setBorderRight(BorderStyle.NONE);
        cell.setCellStyle(borderStyle);
        return cell;
    }

    public static Cell addBottomBorder(Workbook workbook, Cell cell) {
        CellStyle borderStyle = cell.getCellStyle();
        borderStyle.setBorderTop(BorderStyle.NONE);
        borderStyle.setBorderBottom(BorderStyle.THIN);
        borderStyle.setBorderLeft(BorderStyle.NONE);
        borderStyle.setBorderRight(BorderStyle.NONE);
        cell.setCellStyle(borderStyle);
        return cell;
    }



    public static Cell setFontSizeAndColor(Workbook workbook, Cell cell, int fontSize,String fontName, String fontColorName , boolean isBold , boolean isItalic) {
        // Create a cell style with font settings
        CellStyle style = cell.getCellStyle();

        // Create a font object and set the font size
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) fontSize);
        font.setFontName(fontName);
        font.setBold(isBold);
        font.setItalic(isItalic);
        // Set the font color using the color name
        font.setColor(IndexedColors.valueOf(fontColorName).getIndex());

        style.setFont(font);

        // Apply the cell style to the cell
        cell.setCellStyle(style);

        return cell;
    }

    public static List<Cell> createDynamicCell(Sheet sheet, int firstRow, int lastRow, int firstCol, int lastCol, String value , String fontName , int fontSize , String fontColor , String backGroundColor , boolean leftBorder , boolean rightBorder , boolean topBorder , boolean bottomBorder , HorizontalAlignment h , VerticalAlignment v , boolean isBold , boolean isItalic) {
        List<Cell> mergedCells = new ArrayList<>();

        // Merge the cells
        CellRangeAddress mergedRegion = new CellRangeAddress(firstRow, lastRow, firstCol, lastCol); // Replace with your desired merged cell range
        sheet.addMergedRegion(mergedRegion);

        Workbook workbook = sheet.getWorkbook();
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(h);
        style.setVerticalAlignment(v);
        if(leftBorder){
            style.setBorderLeft(BorderStyle.MEDIUM);
        }
        if(rightBorder){
            style.setBorderRight(BorderStyle.MEDIUM);
        }
        if(topBorder){
            style.setBorderTop(BorderStyle.MEDIUM);
        }
        if(bottomBorder){
            style.setBorderBottom(BorderStyle.MEDIUM);
        }
        if(!backGroundColor.trim().equals("")){
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            style.setFillForegroundColor(IndexedColors.valueOf(backGroundColor).getIndex());
        }

        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) fontSize);
        if(!fontName.trim().equals("")){
            font.setFontName(fontName);

        }
        font.setBold(isBold);
        font.setItalic(isItalic);
        // Set the font color using the color name
        if(!fontColor.trim().equals("")){
            font.setColor(IndexedColors.valueOf(fontColor).getIndex());

        }
        style.setFont(font);

        // Iterate through the merged region and set the value for each cell
        for (int rowIndex = firstRow; rowIndex <= lastRow; rowIndex++) {
            Row row = sheet.getRow(rowIndex);
            if (row == null) {
                row = sheet.createRow(rowIndex);
            }

            for (int colIndex = firstCol; colIndex <= lastCol; colIndex++) {
                Cell cell = row.getCell(colIndex);
                if (cell == null) {
                    cell = row.createCell(colIndex);
                }

                style.setWrapText(true);
                cell.setCellStyle(style);
                // Set the value for the cell
                cell.setCellValue(value);
                // Add the cell to the list of merged cells
                mergedCells.add(cell);
                //sheet.addMergedRegion();
                for (int i = mergedRegion.getFirstColumn(); i <= mergedRegion.getLastColumn(); i++) {
                    sheet.setColumnWidth(i, Math.max(sheet.getColumnWidth(i), 10 * 650)); // Set the minimum width (e.g., 10 characters wide)
                    //sheet.autoSizeColumn(i);
                }
            }
        }

        return mergedCells;
    }


}
