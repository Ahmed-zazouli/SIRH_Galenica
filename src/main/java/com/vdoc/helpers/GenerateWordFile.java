package com.vdoc.helpers;

import com.aspose.words.*;
import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.IAttachment;
import com.axemble.vdoc.sdk.interfaces.IResource;
import com.axemble.vdoc.sdk.interfaces.IWorkflowInstance;
import com.axemble.vdoc.sdk.modules.IWorkflowModule;
import com.axemble.vdoc.sdk.utils.StringUtils;
import org.apache.turbine.Turbine;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.*;
import java.util.Map.Entry;

public class GenerateWordFile extends BaseDocumentExtension {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2913421132212918650L;

	protected static final String BOOKMARK_SEPARATOR_TABLEAU = "__";
	protected static final String BOOKMARK_SEPARATOR_RESERVOIR = "_";
	protected static final String VAR_START_TOKEN = "${";
	protected static final String VAR_END_TOKEN = "}";

	protected static final String BOOKMARK_TEMP_NAME = "bookmark_temp";

	/**
	 * Valorisation du document (Un seul pour l'instant)
	 * 
	 * @param iAttachment
	 * @throws Exception
	 */
	public InputStream valorization(IWorkflowModule workflowModule, IWorkflowInstance workflowInstance, IAttachment iAttachment) throws Exception {
		// Gestion de la licence
		License license = new License();

		String licensePath = workflowModule.getConfiguration().getStringProperty("com.vdoc.connector.aspose.licence.path");

		if (StringUtils.isNotEmpty(licensePath)) {
			license.setLicense(Turbine.getRealPath(licensePath));
		}

		Document doc = new Document(iAttachment.getInputStream());
		DocumentBuilder builder = new DocumentBuilder(doc);

		bookmarkValorization(doc, builder , workflowModule , workflowInstance);

		return getInputStream(doc);
	}

	/**
	 * @param doc
	 * @param builder
	 * @throws Exception
	 */
	private void bookmarkValorization(Document doc, DocumentBuilder builder , IWorkflowModule workflowModule, IWorkflowInstance workflowInstance) throws Exception
	{
		//Map contenant les signets entête des tableaux word
		Map<String, Collection<Bookmark>> bookmarkHeadersMap = new HashMap<>();

		BookmarkCollection bookmarkCollection = doc.getRange().getBookmarks();
		Iterator<Bookmark> bookmarkIterator = bookmarkCollection.iterator();

		while (bookmarkIterator.hasNext())
		{
						// Récupération du signet
			Bookmark bookmark = bookmarkIterator.next();
			if(!bookmark.getName().equals("_Hlk26804076"))
			{
				LOGGER.error(bookmark.getName());
				// Vider le texte du signet
				if(!bookmark.getName().equals("_GoBack"))
				{
					bookmark.setText("");
				}
				// Si lié aux tableaux dynamiques/sous-processus
				if (bookmark.getName().contains(BOOKMARK_SEPARATOR_TABLEAU))
				{
					// Nom système du champ tableau VDoc
					String tabSysName = bookmark.getName().split(BOOKMARK_SEPARATOR_TABLEAU)[0];
					
					Collection<Bookmark> bookmarkHeaders = bookmarkHeadersMap.get(tabSysName);
					
					if (bookmarkHeaders == null)
					{
						bookmarkHeaders = new ArrayList<>();
					}
					
					bookmarkHeaders.add(bookmark);
					bookmarkHeadersMap.put(tabSysName, bookmarkHeaders);
				}
				else
				{
					// Récupération de la valeur du champ correspondant au signet
					Object resourceValue = workflowInstance.getValue(bookmark.getName());
					
					// Remplacer les signets par des valeurs de champs
					if(resourceValue != null)
					{
						VDocValuesHelperForBookmarks.setType(resourceValue, bookmark, builder, workflowModule);
					}
				}
			}
			
			
		}
		//Construction des lignes des tableaux word
		buildTableRows(bookmarkHeadersMap, doc, builder , workflowModule , workflowInstance);
	}


	public static InputStream getInputStream(Document document) throws Exception {
		ByteArrayOutputStream bos = null;
		ByteArrayInputStream bis = null;

		try {

			int saveFormat = document.getOriginalLoadFormat();

			bos = new ByteArrayOutputStream();
			document.save(bos, saveFormat);
			bis = new ByteArrayInputStream(bos.toByteArray());

			return bis;

		} finally {
			// Closer les stream
			bos.flush();
			bos.close();
		}
	}
	
	/**
	 * Construction des lignes liées aux tableaux VDoc<br>
	 * Les colonnes doivent être déjà définies
	 * 
	 * @param bookmarkHeadersMap
	 * @param doc
	 * @param builder
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	private void buildTableRows(Map<String, Collection<Bookmark>> bookmarkHeadersMap, Document doc, DocumentBuilder builder , IWorkflowModule workflowModule , IWorkflowInstance workflowInstance) throws Exception
	{

		for (Entry<String, Collection<Bookmark>> entry : bookmarkHeadersMap.entrySet())
		{
			Table table = null;

			Collection<Bookmark> bookmarkHeaders = entry.getValue();

			// We get parentcell and table
			Cell parentCell = (Cell)bookmarkHeaders.iterator().next().getBookmarkStart().getAncestor(NodeType.CELL);
			if (parentCell != null)
			{
					table = parentCell.getParentRow().getParentTable();

				//Les lignes du champ tableau VDoc
				Collection<IResource> resources = (Collection<IResource>)workflowInstance.getValue(entry.getKey());
				
				
				
				if (resources != null)
				{

					for (IResource resource : resources)
					{
						// We build a new row for each lines in VDoc table
						Row newRow = new Row(doc);
						newRow.getRowFormat().setAllowBreakAcrossPages(true);
						table.appendChild(newRow);

						for (Bookmark bookmarkTableCellHeader : bookmarkHeaders)
						{
							Cell cellHeader = (Cell)bookmarkTableCellHeader.getBookmarkStart().getAncestor(NodeType.CELL);

							//SousChamp
							String propertyName = bookmarkTableCellHeader.getName().split(BOOKMARK_SEPARATOR_TABLEAU)[1];
							Object resourceValue = resource.getValue(propertyName);

							//Libellé
							String label = "";
							if(resource!=null && resource.getDefinition()!=null && resource.getDefinition().getProperty(propertyName)!=null){
								label = resource.getDefinition().getProperty(propertyName).getLabel();
							}
							bookmarkTableCellHeader.setText(label);

							//Création de la cellule
							Cell cell = new Cell(doc);
							//Redéfinir la même taille sur les nouvelles cells
							cell.getCellFormat().setWidth(cellHeader.getCellFormat().getWidth());
							newRow.appendChild(cell);
							cell.appendChild(new Paragraph(doc));

							//Création d'un signet temporaire
							cell.getFirstParagraph().appendChild(new BookmarkStart(doc, BOOKMARK_TEMP_NAME));
							cell.getFirstParagraph().appendChild(new Run(doc, BOOKMARK_TEMP_NAME));
							cell.getFirstParagraph().appendChild(new BookmarkEnd(doc, BOOKMARK_TEMP_NAME));

							//Forcer un alignement dans le tableau
							if (resourceValue instanceof Number)
							{
								cell.getFirstParagraph().getParagraphFormat().setAlignment(ParagraphAlignment.RIGHT);
							}
							else
							{
								cell.getFirstParagraph().getParagraphFormat().setAlignment(ParagraphAlignment.LEFT);
							}

							//Valorisation
							Bookmark bookmarkTemp = doc.getRange().getBookmarks().get(BOOKMARK_TEMP_NAME);
							if(bookmarkTemp!=null){
								VDocValuesHelperForBookmarks.setType(resourceValue, bookmarkTemp, builder, workflowModule);
								bookmarkTemp.remove();
								}

						}
					}
				
//					if(entry.getKey().equals("Decisions"))
//					{
//						IContext context = workflowModule.getSysadminContext();
//						IViewController controller = workflowModule.getViewController(context);
//						controller.addEqualsConstraint("CodeReunion", workflowInstance.getValue("CodeReunion"));
//						IWorkflow workflow = workflowModule.getWorkflow(context, workflowInstance.getCatalog(), "Decisions_1.0");
//						controller.setOrberBy("Nature_T", String.class, true);
//						Collection<IWorkflowInstance> collection = controller.evaluate(workflow);
//						
//						for (IResource resource : collection) 
//						{
//							// We build a new row for each lines in VDoc table
//							Row newRow = new Row(doc);
//							newRow.getRowFormat().setAllowBreakAcrossPages(true);
//							table.appendChild(newRow);
//							
//							for (Bookmark bookmarkTableCellHeader : bookmarkHeaders)
//							{
//								Cell cellHeader = (Cell)bookmarkTableCellHeader.getBookmarkStart().getAncestor(NodeType.CELL);
//
//								//SousChamp
//								String propertyName = bookmarkTableCellHeader.getName().split(BOOKMARK_SEPARATOR_TABLEAU)[1];
//								Object resourceValue = resource.getValue(propertyName);
//
//								//Libellé
//								String label = resource.getDefinition().getProperty(propertyName).getLabel();
//								bookmarkTableCellHeader.setText(label);
//
//								//Création de la cellule
//								Cell cell = new Cell(doc);
//								//Redéfinir la même taille sur les nouvelles cells
//								cell.getCellFormat().setWidth(cellHeader.getCellFormat().getWidth());
//								newRow.appendChild(cell);
//								cell.appendChild(new Paragraph(doc));
//
//								//Création d'un signet temporaire
//								cell.getFirstParagraph().appendChild(new BookmarkStart(doc, BOOKMARK_TEMP_NAME));
//								cell.getFirstParagraph().appendChild(new Run(doc, BOOKMARK_TEMP_NAME));
//								cell.getFirstParagraph().appendChild(new BookmarkEnd(doc, BOOKMARK_TEMP_NAME));
//
//								//Forcer un alignement dans le tableau
//								if (resourceValue instanceof Number)
//								{
//									cell.getFirstParagraph().getParagraphFormat().setAlignment(ParagraphAlignment.RIGHT);
//								}
//								else
//								{
//									cell.getFirstParagraph().getParagraphFormat().setAlignment(ParagraphAlignment.LEFT);
//								}
//
//								//Valorisation
//								Bookmark bookmarkTemp = doc.getRange().getBookmarks().get(BOOKMARK_TEMP_NAME);
//								VDocValuesHelperForBookmarks.setType(resourceValue, bookmarkTemp, builder, workflowModule);
//								bookmarkTemp.remove();
//							}
//							
//						}
//						
//					}
//					else
//					{}
					
				}
			}
		}
	}
}
