package com.moovapps.ibb.rh.GestionPaieV2.Excel;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class ExcelFileBuilder
{
	
	public static class Structure {

		@JsonProperty("columns")
		public List<Column> columns = null;

		public Structure() {
		}

		public Structure(List<Column> columns) {
			this.columns = columns;
		}
	}

	public static class Column {

		@JsonProperty("libele")
		public String libele;
		@JsonProperty("dataType")
		public String dataType;
		@JsonProperty("method")
		public String method;
		@JsonProperty("columns")
		public List<Column> columns = null;

		public Column() {
		}

		public Column(String libele, String dataType, String method,
				List<Column> columns) {
			this.libele = libele;
			this.dataType = dataType;
			this.method = method;
			this.columns = columns;
		}

	}
	
	int cellIndex = 0;
    public ExcelFileBuilder(){
    	
    	XSSFWorkbook workbook = null;
    	XSSFSheet firstSheet = null;
    	ObjectMapper mapper = new ObjectMapper();
    	Structure struct = null;
    	try {
    		workbook = new XSSFWorkbook(new FileInputStream(new File("C:\\TEST\\BuildExcel.xlsx")));
    		firstSheet = workbook.getSheetAt(0);
    		
    		String jsonStructure = readAllBytesJava7("F:\\workspace\\IBB_RH\\src\\com\\moovapps\\ibb\\rh\\GestionPaieV2\\Excel\\ExcelFileStructure.json");
			struct = mapper.readValue(jsonStructure, Structure.class);
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
    	cellIndex = 0;
    	buildColumns(struct.columns, 0, firstSheet);
    	
	}
    
    private void buildColumns(List<Column> cols, int rowIndex, XSSFSheet sheet) {
    	
    	
    	for (Column column : cols) {
    		sheet.createRow(rowIndex).createCell(cellIndex).setCellValue(column.libele);
			if(column.columns != null && column.columns.size() > 0){
				buildColumns(column.columns, rowIndex+1, sheet);
			}else {
				cellIndex++;
			}
		}
	}
    
    private static String readAllBytesJava7(String filePath) 
    {
        String content = "";
        try {
            content = new String ( Files.readAllBytes( Paths.get(filePath) ) );
        } catch (IOException e) 
        {
            e.printStackTrace();
        }
 
        return content;
    }
}