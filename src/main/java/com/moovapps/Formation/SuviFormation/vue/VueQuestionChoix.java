package com.moovapps.Formation.SuviFormation.vue;

import com.axemble.studio.providers.workflowInstances.views.WorkflowInstanceViewProvider;
import com.axemble.vdoc.directory.domain.User;
import com.axemble.vdoc.sdk.interfaces.runtime.INavigateContext;
import com.axemble.vdoc.sdk.view.extensions.BaseViewExtension;
import com.axemble.vdoc.sdk.view.extensions.ViewItem;
import com.axemble.vdp.catalog.domain.Catalog;
import com.axemble.vdp.resource.domain.Property;
import com.axemble.vdp.resource.domain.ResourceDefinition;
import com.axemble.vdp.ui.core.providers.views.XMLViewProvider;
import com.axemble.vdp.ui.framework.composites.base.CtlAbstractView;
import com.axemble.vdp.ui.framework.composites.base.models.views.XMLViewModel;
import com.axemble.vdp.view.request.IRequestGenerator;
import com.axemble.vdp.view.request.constraints.Constraint;
import com.axemble.vdp.view.request.constraints.ConstraintGroup;
import com.axemble.vdp.view.transformer.IViewTransformer;
import com.axemble.vdp.views.query.Column;
import com.axemble.vdp.views.query.Definition;
import com.axemble.vdp.workflow.domain.WorkflowContainer;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class VueQuestionChoix extends BaseViewExtension implements IRequestGenerator {
    protected List<Column> definitionColumns;
    protected IViewTransformer viewTransformer;
    protected CtlAbstractView ctlAbstractView ;
    @Override
    public String onPrepareSQL(String sqlQuery){
        //sqlQuery = sqlQuery.replace("( ? =", "( ?=5 and r.Departement in");

        //XMLViewModel model = new XMLViewModel( definitionColumns, viewTransformer);
       // model.setAllowDelete(true);

       // new WorkflowInstanceViewProvider((INavigateContext) getWorkflowModule().getSysadminContext(),ctlAbstractView);

        //XMLViewProvider model = new XMLViewProvider();

        return super.onPrepareSQL(sqlQuery);
    }

    @Override
    public void onPrepareColumns(List viewModelColumns) {
        definitionColumns = viewModelColumns;
        super.onPrepareColumns(viewModelColumns);
    }

    @Override
    public void addConstraint(Constraint constraint) {

    }

    @Override
    public void addAdditionalConstraint(Constraint constraint) {

    }

    @Override
    public void clearConstraints() {

    }

    @Override
    public void clearAdditionalConstraints() {

    }

    @Override
    public ConstraintGroup getConstraints() {
        return null;
    }

    @Override
    public ConstraintGroup getAdditionalConstraints() {
        return null;
    }

    @Override
    public boolean isColumnSortable(String propertyCompleteName) {
        return false;
    }

    @Override
    public boolean isColumnFilterable(String propertyCompleteName) {
        return false;
    }

    @Override
    public boolean isSecured() {
        return true;
    }

    @Override
    public void setSecured(boolean secured) {

    }

    @Override
    public int getCountElements() {
        return 0;
    }

    @Override
    public Map<Object, Integer> getGroupByCount(String completePropertyName) {
        return null;
    }

    @Override
    public List<Serializable[]> getResults() {
        return null;
    }

    @Override
    public User getConnectedUser() {
        return null;
    }

    @Override
    public void setConnectedUser(User connectedUser) {

    }

    @Override
    public String getLanguage() {
        return null;
    }

    @Override
    public void setLanguage(String language) {

    }

    @Override
    public String getOrderBy() {
        return null;
    }

    @Override
    public void setOrderBy(String orderBy) {

    }

    @Override
    public boolean isSortAsc() {
        return false;
    }

    @Override
    public void setSortAsc(boolean sortAsc) {

    }

    @Override
    public int getStart() {
        return 0;
    }

    @Override
    public void setStart(int start) {

    }

    @Override
    public int getCount() {
        return 0;
    }

    @Override
    public void setCount(int count) {

    }

    @Override
    public Map<String, Integer> getSelectColumnsIndexes() {
        return null;
    }

    @Override
    public int getSelectColumnIndex(String propertyName) {
        return 0;
    }

    @Override
    public Map<String, Property> getCatalogProperties() {
        return null;
    }

    @Override
    public Catalog getCurrentCatalog() {
        return null;
    }

    @Override
    public ResourceDefinition getCurrentResourceDefinition() {
        return null;
    }

    @Override
    public WorkflowContainer getCurrentWorkflowContainer() {
        return null;
    }

    @Override
    public String getMainTable() {
        return null;
    }

    @Override
    public Object evaluateFunctionValue(String function) {
        return null;
    }

    @Override
    public Collection<String> getColumnsToDisplay() {
        return null;
    }

    @Override
    public void setColumnsToDisplay(Collection<String> columnsToDisplay) {

    }

    @Override
    public boolean onPrepareView(Definition viewDefinition) {

        //ProcessViewTransformer viewTransformer = new ProcessViewTransformer( requestGenerator, getLanguage() );
        return super.onPrepareView(viewDefinition);
    }

    @Override
    public boolean onReady() {
        return super.onReady();
    }

    @Override
    public void onPrepareItem(ViewItem item) {
    //        ctlAbstractView = (CtlAbstractView) item ;
    }
}
