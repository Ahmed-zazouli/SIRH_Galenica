package com.moovapps.ibb.rh.GestionPaieV2.Excel;

import org.apache.chemistry.opencmis.commons.impl.json.JSONObject;

public class Colonnes {
	JSONObject colonnes = new JSONObject();
	
	public Colonnes() {
		new ExcelFileBuilder();
	}
}
