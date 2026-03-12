package com.moovapps.EVALUATION.ButtonsHTML;

import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public  class CellStyleMap {
    private XSSFWorkbook workbook;

    public CellStyleMap(XSSFWorkbook workbook) {
        this.workbook = workbook;
    }

    public XSSFCellStyle createCellStyle(XSSFCellStyle sourceStyle) {
        XSSFCellStyle newCellStyle = workbook.createCellStyle();
        newCellStyle.cloneStyleFrom(sourceStyle);
        return newCellStyle;
    }
}

