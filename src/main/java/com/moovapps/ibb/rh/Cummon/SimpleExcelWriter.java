package com.moovapps.ibb.rh.Cummon;

import com.axemble.vdoc.sdk.interfaces.*;
import com.axemble.vdoc.sdk.modules.IDirectoryModule;
import com.axemble.vdoc.sdk.modules.IProjectModule;
import com.axemble.vdoc.sdk.modules.IWorkflowModule;
import org.apache.chemistry.opencmis.commons.impl.json.JSONObject;
import org.apache.commons.io.FileUtils;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.lang.reflect.Method;
import java.util.*;

public class SimpleExcelWriter {

////////////LEGEND ///////////
	
//{
//annuaireField: "xxxx()",
//extendedField: "xxxx",
//methodInThisClass: "_xxxx",
//FormuleField: "$xxxxx",
//FieldInTheSecondDoc: "$x$xxxxx"
//}
	
	JSONObject userDataModel = new JSONObject();

	List<String> colonnes = Arrays.asList("Matricule", "getLastName()",
			"getFirstName()", "$3$M/H", "$4$TauxHoraire",
			"_getUserAbsenceFromUserCongesMap", "_getUserAbsenceFromUserCongesMapByHour", "$5$Reprise",
			"$6$Rappel", "_getUserCongesPayesFromUserCongesMap", "$7$HS25", "$8$HS50",
			"$9$HS100", "_getUserAvanceFromUserMap", "$10$AvanceConges",
			"_getFraisMedicauxFromUserMap", "$11$PrimeBrut", "$12$SalaireNet",
			"$13$Observation", "$14$STC", "$15$STCConges", "$16$STCJTravailles",
			"$17$SituationSalarie", "$18$NouveauSalarie");
	
	IWorkflowModule workflowModule = null;
	IDirectoryModule directoryModule = null;
	IProjectModule projectModule = null;
	IWorkflowInstance currentDoc = null;
	IContext sysContext = null;
	IUser currentUser = null;
	IStorageResource societe = null;
	int periodeCloture = 1;
	int selectedMonthPaie = -1;
	Date paieFrom = null;
	Date paieTo = null;
	
//	HashMap<UserCongeModel, JSONObject> userCongesMap = new HashMap<>();
	HashMap<IUser, JSONObject> userMap = new HashMap<>();
	
	public SimpleExcelWriter(IWorkflowModule workflowModule, IDirectoryModule directoryModule, IProjectModule projectModule,IWorkflowInstance currentDoc){
		userDataModel.put("Absence", null);
		userDataModel.put("CN", null);
		userDataModel.put("CE", null);
		userDataModel.put("CM", null);
		userDataModel.put("SS", null);
		userDataModel.put("Avance", null);
		userDataModel.put("FraixMedicaux", null);
		
		this.workflowModule = workflowModule;
		this.directoryModule = directoryModule;
		this.projectModule = projectModule;
		this.currentDoc = currentDoc;
		this.sysContext = workflowModule.getSysadminContext();
		this.currentUser = workflowModule.getLoggedOnUser();
		this.selectedMonthPaie = Float.valueOf((String)currentDoc.getValue("Mois")).intValue();
		
		String societeName = null;
		if(currentUser.getExtendedAttributes().getValue("Societe") != null){
			societeName = (String)currentUser.getExtendedAttributes().getValue("Societe");
			this.societe = getSociete(societeName);
			if(this.societe != null){
				if(!societe.getValue("ClotureSociete").equals("Mois")){
					periodeCloture = ((Number)societe.getValue("PeriodeDeCloture")).intValue();
				}
			}
		}
		Calendar paieDateFrom = Calendar.getInstance();
		paieDateFrom.set(Calendar.DATE, 1);
		paieDateFrom.set(Calendar.MONTH, selectedMonthPaie);
		if(periodeCloture != 1){
			paieDateFrom.add(Calendar.MONTH, -1);
		}
		paieDateFrom.set(Calendar.DATE, periodeCloture);
		Calendar paieDateTo = Calendar.getInstance();
		paieDateTo.setTime(paieDateFrom.getTime());
		paieDateTo.add(Calendar.MONTH, 1);
		paieDateTo.add(Calendar.DATE, -1);
		paieFrom = paieDateFrom.getTime();
		paieTo = paieDateTo.getTime();
	}
	
	public void buildMaquette(File maquetteFile) throws FileNotFoundException, IOException{
		List<String> maqeutteColonnes = Arrays.asList("Matricule", "getLastName()", "getFirstName()");
		
 		Collection<IUser> myUsers = getUsersWithTheSameSociete(sysContext, directoryModule, currentUser);
		XSSFWorkbook workbook = new XSSFWorkbook(new FileInputStream(maquetteFile));
		XSSFSheet firstSheet = workbook.getSheetAt(0);
		
		BuildCongesByUser();
		
		int line = 2;
		for (IUser user : myUsers) {
			XSSFRow row = firstSheet.createRow(line);
			for (int j = 0; j < maqeutteColonnes.size(); j++) {
				Object value = null;
				String methodName = maqeutteColonnes.get(j).replace("(", "").replace(")", "").replace("_", "").replace("-", "").replace("$", "");
				if(maqeutteColonnes.get(j).endsWith("()")){
					try {
						Method method = user.getClass().getMethod(methodName);
						value = method.invoke(user);
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else {
					value = user.getExtendedAttributes().getValue(methodName);
				}
				
				try {
					row.createCell(j).setCellValue(((Number)value).doubleValue());
				} catch (Exception e) {
					row.createCell(j).setCellValue((String)value);
				}
			}
			line++;
		}
		try (FileOutputStream outputStream = new FileOutputStream("c://TEST//tmpMaquette.xlsx")) {
			workbook.write(outputStream);
			File toDelete = new File("c://TEST//tmpMaquette.xlsx");
			FileUtils.copyFile(toDelete, maquetteFile);
			toDelete.delete();
			toDelete.deleteOnExit();
		}
	}
	
	public void buildDonneesMoovapps(File donneesMoovappsFile) throws FileNotFoundException, IOException{
		
		Collection<IUser> myUsers = getUsersWithTheSameSociete(sysContext, directoryModule, currentUser);
		XSSFWorkbook workbook = new XSSFWorkbook(new FileInputStream(donneesMoovappsFile));
		XSSFSheet firstSheet = workbook.getSheetAt(0);

		BuildCongesByUser();
		buildAvanceByUser();
		buildFraisMedicauxbyUser();
		
		int line = 2;
		for (IUser user : myUsers) {
			XSSFRow row = firstSheet.createRow(line);
			for (int j = 0; j < colonnes.size(); j++) {
				Object value = null;
				String methodName = colonnes.get(j).replace("(", "").replace(")", "").replace("_", "").replace("-", "").replace("$", "");
				if(colonnes.get(j).startsWith("$")){
					continue;
				} else if(colonnes.get(j).startsWith("_")){
					try {
						Method method = this.getClass().getMethod(methodName, IUser.class);
						value = method.invoke(this, user);
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else if(colonnes.get(j).endsWith("()")){
					try {
						Method method = user.getClass().getMethod(methodName);
						value = method.invoke(user);
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else {
					value = user.getExtendedAttributes().getValue(methodName);
				}
				
				try {
					row.createCell(j).setCellValue(((Number)value).doubleValue());
				} catch (Exception e) {
					row.createCell(j).setCellValue((String)value);
				}
				
			}
			line++;
		}
		try (FileOutputStream outputStream = new FileOutputStream("c://TEST//tmpDonneesMoovapps.xlsx")) {
			workbook.write(outputStream);
			File toDelete = new File("c://TEST//tmpDonneesMoovapps.xlsx");
			FileUtils.copyFile(toDelete, donneesMoovappsFile);
			toDelete.delete();
			toDelete.deleteOnExit();
		}
	}
	
//	public void main(File file, String filePath, boolean mergeData) throws IOException {
// 		Collection<IUser> myUsers = getUsersWithTheSameSociete(sysContext, directoryModule, currentUser);
//		String[] filePathDivided = filePath.split("/");
//		XSSFWorkbook workbook = new XSSFWorkbook(new FileInputStream(file));
//		File alimentationVariablePaieFile = new File("c://TEST//tmpComplementVariablePaie.xlsx");
//		XSSFWorkbook alimentationWorkbook = null;
//		try {
//			alimentationWorkbook = new XSSFWorkbook(new FileInputStream(alimentationVariablePaieFile));
//			alimentationWorkbook.getSheetAt(0).getRow(3).getCell(0);
//		} catch (Exception e) {}
//		XSSFSheet firstSheet = workbook.getSheetAt(0);
//		
//		BuildCongesByUser();
//		buildAvanceByUser();
//		buildFraisMedicauxbyUser();
//		
//		int line = 2;
//		for (IUser user : myUsers) {
//			XSSFRow row = firstSheet.createRow(line);
//			for (int j = 0; j < colonnes.size(); j++) {
//				Object value = null;
//				String methodName = colonnes.get(j).replace("(", "").replace(")", "").replace("_", "").replace("-", "").replace("$", "");
//				if(mergeData && alimentationWorkbook != null && colonnes.get(j).startsWith("$")){
//					int alimentationFileIndex = Integer.valueOf(colonnes.get(j).split("\\$")[1]).intValue();
//					value = getValueFromTheOtherFile(alimentationWorkbook, line, alimentationFileIndex);
//				} else if(colonnes.get(j).startsWith("_")){
//					try {
//						Method method = this.getClass().getMethod(methodName, IUser.class);
//						value = method.invoke(this, user);
//					} catch (Exception e) {
//						e.printStackTrace();
//					}
//				} else if(colonnes.get(j).endsWith("()")){
//					try {
//						Method method = user.getClass().getMethod(methodName);
//						value = method.invoke(user);
//					} catch (Exception e) {
//						e.printStackTrace();
//					}
//				} else {
//					value = user.getExtendedAttributes().getValue(methodName);
//				}
//				
//				try {
//					row.createCell(j).setCellValue(((Number)value).doubleValue());
//				} catch (Exception e) {
//					row.createCell(j).setCellValue((String)value);
//				}
//				
//			}
//			line++;
//		}
//		try (FileOutputStream outputStream = new FileOutputStream(
//				"c://TEST//" + filePathDivided[filePathDivided.length - 1])) {
//			workbook.write(outputStream);
//			File toDelete = new File("c://TEST//" + filePathDivided[filePathDivided.length - 1]);
//			toDelete.delete();
//			toDelete.deleteOnExit();
//		}
//		
//	}
	
	private Object getValueFromTheOtherFile(XSSFWorkbook workbook,int rowIndex, int index) {
		double doubleValue = workbook.getSheetAt(0).getRow(rowIndex).getCell(index).getNumericCellValue();
		Object value = null;
		if(doubleValue > 0){
			value = doubleValue;
		}
		return value;
	}
	
	public Object getUserCongesPayesFromUserCongesMap(IUser user){
		if(userMap.get(user) != null){
			return userMap.get(user).get("CN");
		}
		return null;
	}
	
	public Object getUserAbsenceFromUserCongesMap(IUser user){
		if(userMap.get(user) != null){
			return userMap.get(user).get("Absence");
		}
		return null;
	}
	
	public Object getUserAbsenceFromUserCongesMapByHour(IUser user){
		if(userMap.get(user) != null && userMap.get(user).get("Absence") != null){
			return ((Number)userMap.get(user).get("Absence")).floatValue() * (((Number) societe.getValue("durationWorkHours"))
					.floatValue() / 60);
		}
		return null;
	}
		
	public Object getUserAvanceFromUserMap(IUser user){
		if(userMap.get(user) != null){
			return userMap.get(user).get("Avance");
		}
		return null;
	}
	
	public Object getFraisMedicauxFromUserMap(IUser user){
		if(userMap.get(user) != null){
			return userMap.get(user).get("FraisMedicaux");
		}
		return null;
	}
	
	public Object getUserTauxHoraire(IUser user){
		return user.getExtendedAttributes().getValue("TauxHoraire");
	}
	
	private Collection<IUser> getUsersWithTheSameSociete(IContext context, IDirectoryModule directoryModule, IUser currentUser) {
		Collection<IUser> usersWithTheSameSociete = Collections.emptyList();
		usersWithTheSameSociete = (Collection<IUser>)directoryModule.getUsers(context, currentUser.getOrganization());
		return usersWithTheSameSociete;
	}
	
	private Collection<IWorkflowInstance> getAllDossierMaladieByUser() {
		Collection<IWorkflowInstance> collection = Collections.emptyList();
		try {
			IContext sysContext = workflowModule.getSysadminContext();
			IUser connectedUser = workflowModule.getLoggedOnUser();
			IOrganization organization = directoryModule.getOrganization(sysContext, "DefaultOrganization");
			IProject project = projectModule.getProject(sysContext, "DossierMaladie", organization);
			// IContext context = getWorkflowModule().getLoggedOnUserContext();
			ICatalog catalog = workflowModule.getCatalog(sysContext, "DossiersMaladie", project);
			IWorkflow w = workflowModule.getWorkflow(sysContext, catalog, "DossiersMaladie_1.0");
			IViewController controller = workflowModule.getViewController(sysContext);
			controller.addEqualsConstraint("Societe", connectedUser.getExtendedAttributes().getValue("Societe"));
			controller.addInConstraint("DocumentState", new ArrayList<String>(Arrays.asList("Remboursé clôturé")));
			controller.addGreaterConstraint("DateRemboursementCloture", paieFrom);
			controller.addLessConstraint("DateRemboursementCloture", paieTo);
			collection = controller.evaluate(w);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return collection;
	}

	private void BuildCongesByUser() {
		Collection<IWorkflowInstance> allConges = getAllCongesOfUsersInTheSameSociete();
		for (IWorkflowInstance conge : allConges) {
			Date dateDebutConge = (Date)conge.getValue("DateDeDebut");
			Date dateFinConge = (Date)conge.getValue("DateDeFin");
			if (DateHelper.isTwoRangesOverlapped(dateDebutConge, dateFinConge, paieFrom, paieTo)) {
				Date calculStartDate = dateDebutConge;
				Date calculFinDate = dateFinConge;
				boolean isStartDateChanged = false;
				boolean isEndDateChanged = false;
				if(DateHelper.isDateBeforeDate(calculStartDate, paieFrom)){
					calculStartDate = paieFrom;
					isStartDateChanged = true;
				}
				if(DateHelper.isDateAfterDate(calculFinDate, paieTo)){
					calculFinDate = paieTo;
					isEndDateChanged = true;
				}
				
				float nombreJours = (Float) new WorkingDaysNumberCalculator(
						workflowModule, getJoursOuvrables(societe),
						getJoursOuvres(societe)).calculateV2(societe,calculStartDate,
						calculFinDate).get("nbrJoursDemande");
				nombreJours -= calculateTrancheMinusValue(dateDebutConge, dateFinConge, conge, isStartDateChanged, isEndDateChanged);
				
//				UserCongeModel userModel = new UserCongeModel((IUser)conge.getValue("Demandeur"), (String)conge.getValue("TypeDeConge"));
				IUser demandeur = (IUser)conge.getValue("Demandeur");
				String categorieConges = (String)conge.getValue("TypeDeConge");
				JSONObject tmp = new JSONObject();
				for (String key : userDataModel.keySet()) {
				  Object value = userDataModel.get(key);
				  tmp.put(key, value);
				}
//				tmp = new JSONObject();
				if(userMap.containsKey(demandeur)){
					tmp = userMap.get(demandeur);
					if(tmp.get(categorieConges) != null){						
						tmp.put(categorieConges, (Float)tmp.get(categorieConges) + nombreJours);
					}else{
						tmp.put(categorieConges, nombreJours);
					}
				} else {
					tmp.put(categorieConges, nombreJours);
				}
				userMap.put(demandeur, tmp);
			}
		}
	}
	
	private float calculateTrancheMinusValue(Date dateDebutConge, Date dateFinConge, IWorkflowInstance conge, boolean isStartDateChanged, boolean isEndDateChanged) {
		float heureDebutFinMinus = 0;
		if(dateDebutConge.equals(dateFinConge)){
			if(!isStartDateChanged && conge.getValue("DebutConge").equals("DJ")){				
				heureDebutFinMinus += .5;
			}
		} else{
			if(!isStartDateChanged && conge.getValue("DebutConge").equals("DJ")){
				heureDebutFinMinus += .5;
			}
			if(!isEndDateChanged && conge.getValue("FinConge").equals("DJ")){				
				heureDebutFinMinus += .5;
			}
		}
		return heureDebutFinMinus;
	}
	
	public ArrayList<Integer> getJoursOuvrables(IStorageResource societe) {
		ArrayList<Integer> joursOuvrables = new ArrayList<Integer>(Arrays.asList(2,3,4,5,6,7));
		int joursOuvrablesDebut = -1;
		int joursOuvrablesFin = -1;
		
		if(societe.getValue("JoursOuvrablesDebut") != null){
			joursOuvrablesDebut = Integer.parseInt(((String)societe.getValue("JoursOuvrablesDebut")));
		}
		if(societe.getValue("JoursOuvrablesFin") != null){
			joursOuvrablesFin = Integer.parseInt(((String)societe.getValue("JoursOuvrablesFin")));
		}
		if(joursOuvrablesDebut != -1 && joursOuvrablesFin != -1){	
			joursOuvrables.clear();
			int i = joursOuvrablesDebut;
			joursOuvrables.add(i);
			do {
				i = ((i+1)/8) != 0 ? 1 : (i+1);
				joursOuvrables.add(i);
			} while(i != joursOuvrablesFin);
		}
		
		return joursOuvrables;
	}
	
	public ArrayList<Integer> getJoursOuvres(IStorageResource societe) {
		ArrayList<Integer> joursOuvres = new ArrayList<Integer>(Arrays.asList(2,3,4,5,6));
		int joursOuvresDebut = -1;
		int joursOuvresFin = -1;
		
		if(societe.getValue("JoursOuvresDebut") != null){
			joursOuvresDebut = Integer.parseInt(((String)societe.getValue("JoursOuvresDebut")));
		}
		if(societe.getValue("JoursOuvresFin") != null){
			joursOuvresFin = Integer.parseInt(((String)societe.getValue("JoursOuvresFin")));
		}
		if(joursOuvresDebut != -1 && joursOuvresFin != -1){
			joursOuvres.clear();
			int i = joursOuvresDebut;
			joursOuvres.add(joursOuvresDebut);
			do {
				i = ((i+1)/8) != 0 ? 1 : (i+1);
				joursOuvres.add(i);
			} while(i != joursOuvresFin);			
		}
		
		return joursOuvres;
	}
	
	private Collection<IWorkflowInstance> getAllCongesOfUsersInTheSameSociete()
	{
		Collection<IWorkflowInstance> collection = Collections.emptyList();
		try
		{
			IOrganization organization = directoryModule.getOrganization(sysContext, "DefaultOrganization");
			IProject project = projectModule.getProject(sysContext, "Capone", organization);
			ICatalog catalog = workflowModule.getCatalog(sysContext, "RH", project);
			IWorkflowContainer w = workflowModule.getWorkflowContainer(sysContext, catalog, "GestionDeConges");
			IViewController controller = workflowModule.getViewController(sysContext);
			controller.addEqualsConstraint("Societe", currentUser.getExtendedAttributes().getValue("Societe"));
			controller.addEqualsConstraint("DocumentState", "Clôturé");
			collection = controller.evaluate(w);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			
		}
		return collection;
	}
	
	private void buildAvanceByUser() {
		Collection<IWorkflowInstance> allAvances = getAllAvancesOfUsersInTheSameSociete();
		for (IWorkflowInstance avance : allAvances) {
			IUser beneficiaire= (IUser)avance.getValue("Beneficiaire");
//			JSONObject tmp = new JSONObject();
			JSONObject tmp = new JSONObject();
			for (String key : userDataModel.keySet()) {
			  Object value = userDataModel.get(key);
			  tmp.put(key, value);
			}
//			tmp = userDataModel;
			if(userMap.containsKey(beneficiaire)){
				tmp = userMap.get(beneficiaire);
				if(tmp.get("Avance") != null){					
					tmp.put("Avance", ((Number)tmp.get("Avance")).floatValue() + ((Number)avance.getValue("Montant")).floatValue());
				}else{
					tmp.put("Avance", avance.getValue("Montant"));
				}
			}else{
				tmp.put("Avance", avance.getValue("Montant"));
			}
			userMap.put(beneficiaire, tmp);
		}
	}
	
	private void buildFraisMedicauxbyUser() {
		Collection<IWorkflowInstance> allDossiersMaladies = getAllDossierMaladieByUser();
		for (IWorkflowInstance dossierMaladie : allDossiersMaladies) {
			IUser beneficiaire= (IUser)dossierMaladie.getValue("Demandeur");
			JSONObject tmp = new JSONObject();
			for (String key : userDataModel.keySet()) {
			  Object value = userDataModel.get(key);
			  tmp.put(key, value);
			}
//			tmp = userDataModel;
			if(userMap.containsKey(beneficiaire)){
				tmp = userMap.get(beneficiaire);	
				if(tmp.get("FraisMedicaux") != null){					
					tmp.put("FraisMedicaux", ((Number)tmp.get("FraisMedicaux")).floatValue() + ((Number)dossierMaladie.getValue("MontantDesSoins")).floatValue());
				}else{
					tmp.put("FraisMedicaux", dossierMaladie.getValue("MontantDesSoins"));
				}
			}else{
				tmp.put("FraisMedicaux", dossierMaladie.getValue("MontantDesSoins"));
			}
			userMap.put(beneficiaire, tmp);
		}

	}
	
	private Collection<IWorkflowInstance> getAllAvancesOfUsersInTheSameSociete()
	{
		Collection<IWorkflowInstance> collection = Collections.emptyList();
		try
		{
			IContext sysContext = workflowModule.getSysadminContext();
			IUser connectedUser = workflowModule.getLoggedOnUser();
			IOrganization organization = directoryModule.getOrganization(sysContext, "DefaultOrganization");
			IProject project = projectModule.getProject(sysContext, "GestionDesAvances", organization);
			ICatalog catalog = workflowModule.getCatalog(sysContext, "GestionDesAvances", project);
			IWorkflow w = workflowModule.getWorkflow(sysContext, catalog, "GestionDesAvances_1.0");
			IViewController controller = workflowModule.getViewController(sysContext);
			controller.addEqualsConstraint("Societe", connectedUser.getExtendedAttributes().getValue("Societe"));
			controller.addNotEqualsConstraint("DocumentState", "Accordée");
			controller.addGreaterConstraint("sys_CreationDate", paieFrom);
			controller.addLessConstraint("sys_CreationDate", paieTo);
			collection = controller.evaluate(w);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			
		}
		return collection;
	}
	
	private IStorageResource getSociete(String societeName) {
		IStorageResource societe = null;
		if(societeName != null){			
			try {
				IViewController controller = workflowModule.getViewController(sysContext, IResource.class);
				IOrganization organization = directoryModule.getOrganization(sysContext, "DefaultOrganization");
				IProject project = projectModule.getProject(sysContext,"REFERENTIELCOMMUN",organization);
				ICatalog catalog = workflowModule.getCatalog(sysContext,"REFERENTIEL", 4, project);
				IResourceDefinition definition = workflowModule.getResourceDefinition(sysContext, catalog, "Societe");
				controller.addEqualsConstraint("sys_Title", societeName);
				Collection<IStorageResource> societes = controller.evaluate(definition);
				if (!societes.isEmpty()){				
					societe = societes.iterator().next();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return societe;
	}
}
