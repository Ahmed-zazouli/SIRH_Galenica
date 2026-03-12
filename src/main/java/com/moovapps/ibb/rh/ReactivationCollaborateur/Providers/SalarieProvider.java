package com.moovapps.ibb.rh.ReactivationCollaborateur.Providers;

import com.axemble.vdoc.sdk.Modules;
import com.axemble.vdoc.sdk.exceptions.WorkflowModuleException;
import com.axemble.vdoc.sdk.interfaces.*;
import com.axemble.vdoc.sdk.interfaces.runtime.INavigateContext;
import com.axemble.vdoc.sdk.modules.IDirectoryModule;
import com.axemble.vdoc.sdk.providers.BaseViewProvider;
import com.axemble.vdoc.sdk.utils.Logger;
import com.axemble.vdp.ui.core.document.CoreDocument;
import com.axemble.vdp.ui.core.providers.ICollectionModelViewProvider;
import com.axemble.vdp.ui.core.providers.ICollectionViewProvider;
import com.axemble.vdp.ui.framework.composites.base.CtlAbstractView;
import com.axemble.vdp.ui.framework.composites.base.models.views.CollectionViewModel;
import com.axemble.vdp.ui.framework.composites.base.models.views.ViewModelColumn;
import com.axemble.vdp.ui.framework.composites.base.models.views.ViewModelItem;

import java.text.SimpleDateFormat;
import java.util.*;

public class SalarieProvider extends BaseViewProvider implements ICollectionModelViewProvider
{
	private static final long serialVersionUID = 1L;
	protected static final Logger log = Logger.getLogger(SalarieProvider.class);
	SimpleDateFormat simpleFormat = new SimpleDateFormat("dd/MM/yyyy");
	
	public SalarieProvider(INavigateContext context, CtlAbstractView view)
	{
		super(context, view);
	}
	
	// La vu du selecteur
	@Override
	public void init()
	{
		super.init();
		// View model
		CollectionViewModel viewModel = (CollectionViewModel) getModel();
		// Création des colonnes dans le modèle de la vue
		ViewModelColumn modelColumn = new ViewModelColumn("NumeroFacture", "Numéro facture", ViewModelColumn.TYPE_STRING);
		viewModel.addColumn(modelColumn);
		modelColumn = new ViewModelColumn("Client", "Client", ViewModelColumn.TYPE_STRING);
		viewModel.addColumn(modelColumn);
		modelColumn = new ViewModelColumn("BU", "BU", ViewModelColumn.TYPE_STRING);
		viewModel.addColumn(modelColumn);
		modelColumn = new ViewModelColumn("MontantFacture", "Montant facture (MAD)", ViewModelColumn.TYPE_STRING);
		viewModel.addColumn(modelColumn);
		modelColumn = new ViewModelColumn("Reliquat", "Reliquat (MAD)", ViewModelColumn.TYPE_STRING);
		viewModel.addColumn(modelColumn);
		modelColumn = new ViewModelColumn("DateEcheance", "Date échéance", ViewModelColumn.TYPE_STRING);
		viewModel.addColumn(modelColumn);
	}
	
	@Override
	public List<ViewModelItem> getModelItems()
	{
		
		CoreDocument cd = (CoreDocument)getView().getNavigator().getCurrentNavigation().getContext().getParameterObject(ICollectionViewProvider.CURRENT_DOCUMENT);
		IWorkflowInstance wi = cd.getWorkflowInstance();
		
		IDirectoryModule iDirectoryModule = Modules.getDirectoryModule();
		ArrayList<ViewModelItem> cViewModelItem = new ArrayList<ViewModelItem>();
		try
		{
			
			boolean premiumMembers = false;
			
			try {
				if(getWorkflowModule().getLoggedOnUser().isMemberOf(getWorkflowModule().getGroupByName("Admin"), true)){
					premiumMembers = true;
				}  else if(getWorkflowModule().getLoggedOnUser().isMemberOf(getWorkflowModule().getGroupByName("ResponsableRecouvrement"), true)){
					premiumMembers = true;
				} else if(getWorkflowModule().getLoggedOnUser().isMemberOf(getWorkflowModule().getGroupByName("SuperUtilisateur"), true)){
					premiumMembers = true;
				} else if(getWorkflowModule().getLoggedOnUser().isMemberOf(getWorkflowModule().getGroupByName("SuperUtilisateurConsultation"), true)){
					premiumMembers = true;
				}
			} catch (WorkflowModuleException e1) {
				e1.printStackTrace();
			}
			
			Collection<IWorkflowInstance> mesFactures = getMesFactures(premiumMembers,wi);
//			Collection<IWorkflowInstance> mesFactures = getData();
			
			for (IWorkflowInstance facture : mesFactures) {
				ViewModelItem viewModelItem = new ViewModelItem();
				viewModelItem.setKey((String)facture.getValue("NumeroFacture"));
				viewModelItem.setValue("NumeroFacture", (String)facture.getValue("NumeroFacture"));
				viewModelItem.setValue("Client", "Client: " + ((IStorageResource)facture.getValue("Client")).getValue("sys_Title"));
				viewModelItem.setValue("BU", "BU: " + ((IStorageResource)facture.getValue("BU")).getValue("sys_Title"));
				viewModelItem.setValue("MontantFacture", "Montant facture (MAD): " + ((Number)facture.getValue("MontantMAD")).floatValue());
				viewModelItem.setValue("Reliquat", "Reliquat (MAD): " + ((Number)facture.getValue("SoldeClient")).floatValue());
				viewModelItem.setValue("DateEcheance", "Date échéance: " + simpleFormat.format((Date)facture.getValue("EcheanceFacture")));
				cViewModelItem.add(viewModelItem);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			
			Modules.releaseModule(iDirectoryModule);
		}
		return cViewModelItem;
	}
	
	private Collection<IWorkflowInstance> getMesFactures(boolean premiumMembers, IWorkflowInstance wI) {
		Collection<IWorkflowInstance> collection = Collections.emptyList();
		try
		{
			IContext sysContext = getWorkflowModule().getSysadminContext();
			IOrganization organization = getDirectoryModule().getOrganization(sysContext, "DefaultOrganization");
			IProject project = getProjectModule().getProject(sysContext, "S2MRecouvrement", organization);
			ICatalog catalog = getWorkflowModule().getCatalog(sysContext, "Facture", project);
			IWorkflow w = getWorkflowModule().getWorkflow(sysContext, catalog, "Facture_1.0");
			IViewController controller = getWorkflowModule().getViewController(sysContext);
			controller.addEqualsConstraint("TypeFacture", "FA");
			controller.addNotEqualsConstraint("DocumentState", "Clôturé");
			controller.addEqualsConstraint("Client", wI.getValue("Client"));
			collection = controller.evaluate(w);
//			if(!premiumMembers){				
//				ArrayList<IWorkflowInstance> factures = new ArrayList<>();
//				for (IWorkflowInstance facture : collection) {
//					if(facture.getValue("BU") != null && ((IStorageResource)facture.getValue("BU")).getValue("Directeur").equals(getWorkflowModule().getLoggedOnUser())){
//						factures.add(facture);
//					} else if(facture.getValue("Directeur1") != null && facture.getValue("Directeur1").equals(getWorkflowModule().getLoggedOnUser())){
//						factures.add(facture);
//					} else if(facture.getValue("OwnerMetier") != null && facture.getValue("OwnerMetier").equals(getWorkflowModule().getLoggedOnUser())){
//						factures.add(facture);
//					} else if(facture.getValue("Commercial") != null && facture.getValue("Commercial").equals(getWorkflowModule().getLoggedOnUser())){
//						factures.add(facture);
//					}
//				}
//				collection = factures;
//			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return collection;
	}
	
}
