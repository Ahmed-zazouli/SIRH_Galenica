package com.axemble.vdp.ui.core.providers.views;

import com.axemble.commons.filters.*;
import com.axemble.commons.xml.DomHelper;
import com.axemble.vdoc.core.domain.BaseDomain;
import com.axemble.vdoc.core.exceptions.MissingObjectException;
import com.axemble.vdoc.core.exceptions.VDocException;
import com.axemble.vdoc.core.helpers.ProtocolURIHelper;
import com.axemble.vdoc.directory.domain.User;
import com.axemble.vdoc.project.domain.Project;
import com.axemble.vdoc.sdk.ModulesFactory;
import com.axemble.vdoc.sdk.exceptions.WorkflowModuleException;
import com.axemble.vdoc.sdk.interfaces.IProperty;
import com.axemble.vdoc.sdk.interfaces.runtime.INavigateContext;
import com.axemble.vdoc.sdk.interfaces.runtime.IProvider;
import com.axemble.vdp.VDPManagers;
import com.axemble.vdp.localization.domain.DynamicLocalizationsWrapper;
import com.axemble.vdp.localization.interfaces.IFormatService;
import com.axemble.vdp.resource.classes.SysColumns;
import com.axemble.vdp.resource.domain.Property;
import com.axemble.vdp.tools.ConverterService;
import com.axemble.vdp.ui.core.document.CoreDocument;
import com.axemble.vdp.ui.core.providers.forms.ProcessViewFilterFormProvider;
import com.axemble.vdp.ui.core.providers.forms.filters.FilterFormProvider;
import com.axemble.vdp.ui.framework.composites.base.CtlAbstractView;
import com.axemble.vdp.ui.framework.composites.base.models.views.XMLViewModel;
import com.axemble.vdp.ui.framework.document.AbstractDocument;
import com.axemble.vdp.ui.framework.widgets.components.sys.base.ResourceSelectorInputComponent;
import com.axemble.vdp.ui.framework.widgets.components.sys.forms.SelectorInputComponent;
import com.axemble.vdp.ui.framework.widgets.list.Option;
import com.axemble.vdp.utils.StringUtils;
import com.axemble.vdp.utils.XMLUtil;
import com.axemble.vdp.view.classes.ViewFormulaContext;
import com.axemble.vdp.view.classes.ViewHelper;
import com.axemble.vdp.view.domain.StorageResourceDefinitionView;
import com.axemble.vdp.view.domain.View;
import com.axemble.vdp.view.transformer.IViewTransformer;
import com.axemble.vdp.view.transformer.ProcessViewTransformer;
import com.axemble.vdp.views.query.Definition;
import com.axemble.vdp.views.query.Definition.Template;
import com.axemble.vdp.views.query.Field;
import com.axemble.vdp.views.query.Fieldgroup;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.*;

public class XMLViewProvider extends ProcessViewProvider {
    private static final long serialVersionUID = 1L;

    public XMLViewProvider(INavigateContext context, CtlAbstractView view) {
        super(context, view);
    }

    @Override
    public void init() {
        // TODO : g�rer un meilleur moyen de m�moriser les filtres ...
        if ("treatment".equals(this.view.getName())) // Do not memorize filters for default view name
            this.view.setName(null);

        super.init();

        // On recherche une viewDefinition (m�me si elle a d�j� �t� pos�e plus t�t : cela permet d'�tre sur que les classes d'extension sont bien rappel�es)
        if (xmlViewElement != null) { // Try to load definition from XML
            Node viewDefinitionNode = XMLUtil.selectChildNode(xmlViewElement, "definition");
            if (viewDefinitionNode != null)
                this.setViewDefinition(ViewHelper.unmarshallView(viewDefinitionNode));
            else {
                String viewName = xmlViewElement.getAttribute("view");
                if (StringUtils.isEmpty(viewName))
                    viewName = this.context.getParameterString("view");
                View selectorView = null;
                if (getParentSelector() != null && getParentSelector().getSelectorField() instanceof ResourceSelectorInputComponent && ResourceSelectorInputComponent.DATA_DISPLAY_VIEW.equals(getParentSelector().getSelectorField().getDatadisplay())) {
                    selectorView = (View) getParentSelector().getSelectorField().getView();
                    // Si la vue et le XML de vue sont vide il faut charger la vue par d�faut
                    if (selectorView == null && StringUtils.isEmpty(getParentSelector().getSelectorField().getXmlView()) && getParentSelector().getSelectorField().getProperty() != null) {
                        selectorView = VDPManagers.getViewManager().getResourceDefinitionView(StorageResourceDefinitionView.DEFAULT_NAME, ((Property) getParentSelector().getSelectorField().getProperty()).getForeignResourceDefinition());
                        getParentSelector().getSelectorField().setView(selectorView);
                    }

                }

                View pluginView = null;
                if (getRootNavigator().getSiteModule().getExecutionContext() != null && getRootNavigator().getSiteModule().getExecutionContext().getPluginContext() != null) {
                    pluginView = (View) getRootNavigator().getSiteModule().getExecutionContext().getPluginContext().get("view");
                    // On nettoie le cache (sinon il n'est jamais nettoy� : pas n�cessaire pour filecenter, mais process est assez gourmand)
                    getRootNavigator().getSiteModule().getExecutionContext().getPluginContext().clear();
                }

                if (StringUtils.isNotEmpty(viewName) || pluginView != null || selectorView != null) {
                    try {
                        view.setName("XMLViewProvider_" + viewName); // To memorize filters differently for each view
                        if (StringUtils.isNotEmpty(viewName)) {
                            this.setViewObject((View) ProtocolURIHelper.getResource(viewName));
                            if (this.viewObject == null)
                                this.setViewObject(VDPManagers.getViewManager().getView(viewName));
                        } else if (selectorView != null) {
                            this.setViewObject(selectorView);
                            viewName = selectorView.getProtocolURI();
                        } else {
                            this.setViewObject(pluginView); // Cas du plugin
                            this.context.setParameter("usePlugin", "true");
                        }
                        view.setName("XMLViewProvider_" + viewName); // To memorize filters differently for each view

                        if (this.viewObject == null)
                            throw new MissingObjectException("Cannot find view : " + xmlViewElement.getAttribute("view"));
                        else {
                            this.setViewObject(viewObject);
                            this.setViewDefinition(ViewHelper.unmarshallView(viewObject.getXmlDefinition()));
                        }
                    } catch (Exception e) {
                        getNavigator().processErrors(e);
                    }
                }
            }
        }

        if (viewDefinition == null && getParentSelector() != null) {
            Element elementSelectorField = null;
            Node viewDefinitionNode = null;
            if (getParentSelector().getSelectorField() instanceof ResourceSelectorInputComponent && StringUtils.isNotEmpty(getParentSelector().getSelectorField().getXmlView()))
                elementSelectorField = getParentSelector().getSelectorField().getXmlViewElement();
            else elementSelectorField = getParentSelector().getSelectorField().getElement();

            if (elementSelectorField != null) {
                if ("definition".equalsIgnoreCase(elementSelectorField.getNodeName()))
                    viewDefinitionNode = elementSelectorField;
                else
                    viewDefinitionNode = XMLUtil.selectChildNode(elementSelectorField, "definition");// On cherche une d�finition dans le XML du s�lecteur
            }
            if (viewDefinitionNode != null)
                this.setViewDefinition(ViewHelper.unmarshallView(viewDefinitionNode));
        }

        if (viewDefinition != null)
            loadModel();
    }

    /**
     * Load model from attributes
     */
    public void loadModel() {
        // Initialize extension classes
        Definition viewDefinition = this.viewDefinition;

        // Initialize extension classes
        if (StringUtils.isNotEmpty(viewDefinition.getExtensionClasses())) {
            // Clone viewDefinition (because view definition may modify it)
            viewDefinition = ViewHelper.unmarshallView(ViewHelper.marshallView(viewDefinition));
            // Initialize extension classes
            this.setExtensionClasses(viewDefinition.getExtensionClasses());
        } else this.setExtensionClasses(null);

        // Create the model
        Project project = null;
        Map<String, Object> viewContext = new HashMap<String, Object>();

        //#74570 : need to be able to force project (DU selector with custom xml view on other project)
        if (StringUtils.isNotEmpty(this.context.getParameterString("project"))) {
            project = (Project) ProtocolURIHelper.getResource(this.context.getParameterString("project"));
            if (project == null) {
                throw new IllegalStateException("View project uri set to '" + this.context.getParameterString("project") + "' but no project found! ");
            }
        }
        if (project == null && getViewObject() != null) {
            project = getViewObject().getProject();
        }
        AbstractDocument currentDocument = (AbstractDocument) this.context.getParameterObject(IProvider.CURRENT_DOCUMENT); // View can be embedded
        if (currentDocument instanceof CoreDocument) {
            if (project == null) {
                project = ((CoreDocument) currentDocument).getProject();
            }
            try {
                if (((CoreDocument) currentDocument).getWorkflowInstance() != null)
                    viewContext.put(ViewFormulaContext.CONTEXT_IRESOURCE, ModulesFactory.getWorkflowModule().getWorkflowInstance(currentDocument));
                else
                    viewContext.put(ViewFormulaContext.CONTEXT_IRESOURCE, ModulesFactory.getWorkflowModule().getResource(currentDocument));
            } catch (WorkflowModuleException e) {
                throw new VDocException(e);
            }
        }

        IViewTransformer viewTransformer = new ProcessViewTransformer(project, viewDefinition, this.getNavigator().getLoggedOnUser(), this.getNavigator().getApplicationFormat().getLanguage(), true, extensionClasses, viewContext);

        // Create a filter form
        Map<String, Object> filterFormParameters = new HashMap<>();
        filterFormParameters.put(ProcessViewFilterFormProvider.CONTEXT_VIEW_DEFINITION, viewDefinition);
        filterFormParameters.put(ProcessViewFilterFormProvider.CONTEXT_VIEW_OBJECT, viewObject);
        filterFormParameters.put(ProcessViewFilterFormProvider.CONTEXT_VIEW_TRANSFORMER, viewTransformer);

        Document xmlDoc = DomHelper.newDocument();
        ViewHelper.marshall(viewDefinition.getFilters(), xmlDoc);
        xmlDoc.getDocumentElement().setAttribute("provider", ProcessViewFilterFormProvider.class.getName());

        // #21951 search view didn't work in portlet
        // #30404 Le composant "Vues de données: Documents de processus VDoc" ne fonctionne plus
        if (getView().getFilterForm() == null || (viewObject != null && !viewObject.equals(getView().getFilterForm().getContext().getParameterObject("ProcessViewFilterFormProvider.ViewObject")))) {
            getView().buildFilterForm(xmlDoc.getDocumentElement(), filterFormParameters);
        }

        //#39633: Update form filters to concidere filters added on onPrepareView
        if (extensionClasses != null && !extensionClasses.isEmpty()) {
            //Update form filters
            updateFormFilters(viewDefinition);
        }

        // Set the filters
        if (!getView().getFilterFormProvider().hasVisibleFilters()) {
            getView().setAutoexec(true);
            getView().getFilterForm().setHidden(true);
        }

        XMLViewModel model = new XMLViewModel(viewDefinition.getView().getColumn(), viewTransformer, viewDefinition.getView().getGroupBy(), getViewObject(), extensionClasses, viewDefinition.getView().isCountElements());
        if ("true".equals(context.getParameterString("usePlugin")))
            model.setUsePlugin(true);

        initializeModel(model);

        this.setModel(model);

        if (StringUtils.isNotEmpty(viewDefinition.getView().getGroupBy())) { // Handle group links
            this.setGroupable(true);
            initializeGroupLinks(viewDefinition.getView().getGroupBy(), viewTransformer);
            this.view.getGroupLinks().setClearViewFilterOnChange(false); // Keep filters when changing group
        }

        if (getParentSelector() != null && getParentSelector().getSelectorField() != null) {
            SelectorInputComponent selectorInputComponent = getParentSelector().getSelectorField();
            List<String> stringGlobalFilter = new ArrayList<>();
            for (String globalFilterColumn : view.getGlobalFilterColumns()) {
                String javaType = ViewHelper.findColumnJavaType(globalFilterColumn, viewTransformer);
                if (String.class.getName().equals(javaType) || DynamicLocalizationsWrapper.class.getName().equals(javaType))
                    stringGlobalFilter.add(globalFilterColumn);
            }
            // Si le nombre de colonne du global filter � chang� il faut mettre � jour le s�lecteur
            if (stringGlobalFilter.size() != view.getGlobalFilterColumns().size()) {
                if (stringGlobalFilter.size() == 0) {
                    String column = selectorInputComponent.getExistingColumnInViewDefinition(viewDefinition, null, viewTransformer);
                    if (StringUtils.isNotEmpty(column))
                        stringGlobalFilter.add(column);
                }

                if (stringGlobalFilter.size() == 0)
                    throw new VDocException("ResourceSelectorField can't be initialized, no string column found for global filters");

                selectorInputComponent.setColumnLabel(stringGlobalFilter.get(0));
                view.setGlobalFilterColumns(stringGlobalFilter);
            }
        }
    }

    /**
     * Update form filters with viewDefinition
     *
     * @param viewDefinition
     */
    private void updateFormFilters(Definition viewDefinition) {
        if (getView().getFilterForm() != null) {
            FilterFormProvider filterFormProvider = (FilterFormProvider) getView().getFilterFormProvider();
            FilterGroup filterGroup = filterFormProvider.getFilterGroup();
            filterGroup.clearFilters();
            for (Object f : viewDefinition.getFilters().getFieldgroup().getFieldgroupOrField()) {
                Filter filter = updateFormFilter(f);

                if (filter != null) {
                    filterGroup.addFilter(filter);
                }
            }
            filterFormProvider.initFilters(filterGroup);
        }
    }

    /**
     * Update a form filter from field
     *
     * @param f
     */
    private Filter updateFormFilter(Object f) {
        if (f instanceof Field) {
            return buildFilter((Field) f);
        } else if (f instanceof Fieldgroup) {
            return buildFilterGroup((Fieldgroup) f);
        }
        return null;
    }

    private Filter buildFilter(Field field) {
        String fieldName = field.getName();
        String fieldValue = field.getValue();
        String fieldValue2 = field.getValue2();
        String operation = field.getOperation();
        String fieldName2 = field.getField();
        String strOperator = field.getOperator();
        Integer intOperator = getIntegerOperator(strOperator);

        //56905 checking for operation before intOperator because of field.getOperator() defaults to equals
        if (StringUtils.isNotEmpty(operation)) {
            return new OperationFilter(operation);
        } else if (intOperator != null) {
            if (StringUtils.isNotEmpty(fieldName)) {
                if (StringUtils.isNotEmpty(fieldName2)) {
                    return new PropertyToPropertyFilter(fieldName, intOperator, fieldName2);
                }
                //56905 checking null instead of empty because sometimes "" is meaningful in filters
                else if (fieldValue != null) {
                    return new PropertyFilter(fieldName, intOperator, fieldValue);
                }
            }
            if (StringUtils.isNotEmpty(fieldValue) && StringUtils.isNotEmpty(fieldValue2)) {
                return new ValueToValueFilter(fieldValue, intOperator, fieldValue2);
            }
        }
        return null;
    }

    private Integer getIntegerOperator(String strOperator) {
        Integer intOperator = null;
        if (Operator.checkFormWb(strOperator)) {
            intOperator = Operator.formWb(strOperator);
        }
        if (intOperator == null) {
            intOperator = Filter.getOperatorCode(strOperator);
        }
        return intOperator;
    }

    private Filter buildFilterGroup(Fieldgroup fieldGroup) {
        String strOperator = fieldGroup.getOperator();
        Integer intOperator = FilterGroup.AND;

        if ("OR".equals(strOperator)) {
            intOperator = FilterGroup.OR;
        }

        FilterGroup filterGroup = new FilterGroup(intOperator);

        for (Object field : fieldGroup.getFieldgroupOrField()) {
            Filter filter = updateFormFilter(field);

            if (filter != null) {
                filterGroup.addFilter(filter);
            }
        }
        return filterGroup;
    }

    protected void initializeModel(XMLViewModel model) {
        try {

            model.setAdditionalLinkParameters(this.additionalLinkParameters);
            model.setAllowCreate(this.canCreate());
            model.setAllowOpen(this.canOpen());
            model.setAllowDelete(this.canDelete());
            model.setAllowSecurity(this.canAccessSecurity());
            model.initializeViewButtons(this);

            Template template = null;
            if (viewDefinition.getTemplate() != null) {
                String templateName = getNavigator().isMobile() ? "mobile" : "desktop";
                for (Template t : viewDefinition.getTemplate()) {
                    if (templateName.equals(t.getType())) {
                        template = t;
                        break;
                    }
                }
            }
            if (template != null && StringUtils.isNotEmpty(template.getValue()))
                getView().setCustomItemTemplate(template.getValue());

            boolean indexView = StringUtils.isNotEmpty(viewDefinition.getIndex());
            getView().setAuthorizeGoLastPage(!indexView); // Pas d'acc�s � la derni�re page pour les r�sultats de recherche
        } catch (Exception e) {
            getNavigator().processErrors(e, true);
        }
    }

    /**
     * Initialize group links
     *
     * @return true : if the viewDefinition has been changed
     */
    private void initializeGroupLinks(String groupByColumn, IViewTransformer viewTransformer) {
        try {
            this.view.getGroupLinks().setLabel(ViewHelper.findColumnLabel(groupByColumn, null, getViewObject(), viewTransformer.getCurrentCatalog(), viewTransformer.getLanguage()));
            this.view.getGroupLinks().clearOptions();
            viewTransformer.getRequestGenerator().clearAdditionalConstraints(); // Remove filters to count elements

            initializeViewGroupLinks(groupByColumn, viewTransformer);

            if (this.view.getGroupLinks().getSelectedKey() == null || !this.view.getGroupLinks().isSelectionValid())
                this.view.getGroupLinks().setSelectedIndex(0, false);
        } catch (Exception e) {
            getNavigator().processErrors(e, true);
        }
    }

    private void initializeViewGroupLinks(String groupByColumn, IViewTransformer viewTransformer) throws Exception {
        int total = 0;
        Map<Object, Integer> results = viewTransformer.getGroupByCount(groupByColumn);

        int unsetCount = -1;
        for (Iterator<Object> iterGroupBy = results.keySet().iterator(); iterGroupBy.hasNext(); ) {
            Object value = iterGroupBy.next();

            int count = (results.get(value)).intValue();
            total += count;

            if (value.equals(""))
                unsetCount = count;
            else
                this.view.getGroupLinks().addOption(new Option(value, groupByFormat(groupByColumn, value, viewTransformer)), count);
        }

        if (unsetCount > -1) // unset Count must be just before the total
            this.view.getGroupLinks().addOption(new Option("", getStaticString("LG_UNSET")), unsetCount);

        if (total > 0 && this.view.getGroupLinks().getOptions().size() > 1) { // Add total option
            Property groupByProperty = viewTransformer.getCatalogProperties().get(groupByColumn);
            if (groupByProperty != null && groupByProperty.getType() == IProperty.IType.COLLECTION && groupByProperty.getList() != null) { // Le total est diff�rent pour les collections multiples
                total = viewTransformer.getCountElements();
            }

            this.view.getGroupLinks().addOption(new Option("*", this.getStaticString("LG_VIEW_GROUP_BY_ALL")), total);
        }
    }

    private String groupByFormat(String groupByColumn, Object value, IViewTransformer viewTransformer) throws Exception {
        if (groupByColumn.equals(SysColumns.SYS_WORKFLOW_CONTAINER))
            return getDynamicLabel(VDPManagers.getWorkflowManager().getWorkflowContainerByName(value.toString(), viewTransformer.getCurrentCatalog()));

        else if (groupByColumn.equals(SysColumns.SYS_STATUS))
            return ViewHelper.renderStatus(Integer.parseInt((String) value)); // Le group by renvoi toujours des String en cl� pour l'instant, car on ne connait pas le type (cf dans ViewManagerBean : // Pour les nvarchar, le getObject ne fonctionne pas (il renvoie un blob) : on teste donc d'abord un getString)

        else if (groupByColumn.equals(SysColumns.SYS_CREATOR))
            return ((User) ProtocolURIHelper.getResource((String) value)).getFullName();

        else if (value!=null &&  value instanceof Date)
            return ConverterService.convertOnlyDate((Date) value, getNavigator().getUserFormat(), IFormatService.SHORT);

        else if (value!=null && value instanceof String) // It may be a protocol URI
        {
            Property prop = viewTransformer.getCatalogProperties().get(groupByColumn);
            if (prop != null && prop.isProtocolUri()) {
                Object target = ProtocolURIHelper.getResource((String) value);
                if (target != null)
                    return ProtocolURIHelper.getResourceLabel(target, viewTransformer.getLanguage());
            } else if (prop != null && prop.getList() != null) // Translate list option
                return prop.getList().getValueLabel((String) value, getLanguage());
        }
        return ConverterService.convert(value, getNavigator().getUserFormat());
    }

    @Override
    public void onGroupLinkChange(Object key) {
        super.onGroupLinkChange(key);
        this.getColumns();
        this.getItems();
    }

    @Override
    protected void delete(List<?> itemsToRemove) {
        for (Object objectUri : itemsToRemove) {
            try {
                BaseDomain object = (BaseDomain) ProtocolURIHelper.getResource((String) objectUri);
                if (object != null) // Cas du sous processus ou le doc a d�j� �t� supprim�
                    object.delete(getRootNavigator().getContext());
            } catch (Exception e) {
                getNavigator().processErrors(e, true);
            }
        }
        this.refresh();
    }

    @Override
    public void onRefresh() {
        if (viewDefinition != null && StringUtils.isNotEmpty(viewDefinition.getView().getGroupBy()))
            initializeGroupLinks(viewDefinition.getView().getGroupBy(), ((XMLViewModel) this.getModel()).getViewTransformer());
        super.onRefresh(); // Will refresh the items
    }
}