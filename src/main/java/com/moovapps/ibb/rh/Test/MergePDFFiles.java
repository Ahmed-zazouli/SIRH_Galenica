package com.moovapps.ibb.rh.Test;

import com.aspose.pdf.facades.PdfFileEditor;
import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;

import java.io.*;

public class MergePDFFiles extends BaseDocumentExtension {
	
	@Override
	public boolean onAfterLoad() {
		
		PdfFileEditor fileEditor = new PdfFileEditor();
		try {
			InputStream stream1 = new FileInputStream(new File("c://TEST//pdf-exemple.pdf"));
			InputStream stream2 = new FileInputStream(new File("c://TEST//sample.pdf"));
			OutputStream outstream = new FileOutputStream("c://TEST//merged.pdf");
			fileEditor.concatenate(stream1, stream2, outstream);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return super.onAfterLoad();
	}

}
