package com.moovapps.EVALUATION.Views;

import com.axemble.vdoc.sdk.interfaces.runtime.INavigateContext;
import com.axemble.vdoc.sdk.providers.BaseViewProvider;
import com.axemble.vdp.localization.interfaces.IDomainLocalizable;
import com.axemble.vdp.ui.framework.components.events.ActionEvent;
import com.axemble.vdp.ui.framework.components.listeners.ChangeListener;
import com.axemble.vdp.ui.framework.composites.base.CtlAbstractView;
import com.axemble.vdp.ui.framework.composites.base.models.views.IViewModel;
import com.axemble.vdp.ui.framework.composites.base.models.views.ViewModelItem;
import com.axemble.vdp.ui.framework.composites.xml.XMLSelector;
import com.axemble.vdp.ui.framework.foundation.Component;
import com.axemble.vdp.ui.framework.foundation.Navigator;
import com.axemble.vdp.ui.framework.foundation.RootNavigator;
import com.axemble.vdp.ui.framework.foundation.Widget;
import com.axemble.vdp.ui.framework.widgets.CtlDiv;
import com.axemble.vdp.ui.framework.widgets.CtlListView;
import com.axemble.vdp.ui.framework.widgets.CtlText;
import com.axemble.vdp.ui.framework.widgets.list.Option;
import com.axemble.vdp.ui.navigation.NavigationObject;
import org.w3c.dom.Element;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class FirstColumnTDClickableV2 extends BaseViewProvider {

    public FirstColumnTDClickableV2(INavigateContext context, CtlAbstractView view) {
        super(context, view);
    }

    @Override
    public void init() {
        super.init();
    }

    @Override
    public void getColumns() {
        super.getColumns();
    }

    @Override
    protected void initializeColumnsDefaults() {
        super.initializeColumnsDefaults();
    }

    @Override
    public boolean isGroupable() {
        return super.isGroupable();
    }

    @Override
    public void setGroupable(boolean isGroupable) {
        super.setGroupable(isGroupable);
    }

    @Override
    public boolean isGroupableSecond() {
        return super.isGroupableSecond();
    }

    @Override
    public void setGroupableSecond(boolean isGroupableSecond) {
        super.setGroupableSecond(isGroupableSecond);
    }

    @Override
    public void getItems() {
        super.getItems();
    }

    @Override
    protected boolean displayCheckbox() {
        return super.displayCheckbox();
    }

    @Override
    protected boolean displayFirstColumn() {
        return super.displayFirstColumn();
    }

    @Override
    public CtlListView.Item buildItem(Widget widget, Object objectName) {
        return super.buildItem(widget, objectName);
    }

    @Override
    public CtlListView.Item buildItem(Widget widget, Object key, ViewModelItem viewModelItem) {
        return super.buildItem(widget, key, viewModelItem);
    }

    @Override
    public Map<String, Widget> getButtons(CtlListView.Item item) {
        return super.getButtons(item);
    }

    @Override
    protected void setEnableCheckBox(CtlListView.Item item, boolean enabled) {
        super.setEnableCheckBox(item, enabled);
    }

    @Override
    protected List<Object> getItemsToRemove(ActionEvent event) {
        return super.getItemsToRemove(event);
    }

    @Override
    protected List<CtlListView.Item> getSelectItems() {
        return super.getSelectItems();
    }

    @Override
    public boolean isFilterContainerHidden() {
        return super.isFilterContainerHidden();
    }

    @Override
    public void setFilterContainerHidden(boolean hidden) {
        super.setFilterContainerHidden(hidden);
    }

    @Override
    public void onViewChanged(Option option) {
        super.onViewChanged(option);
    }

    @Override
    public boolean validatePage(int page) {
        return super.validatePage(page);
    }

    @Override
    public void onPageChanged(int nbOfRows) {
        super.onPageChanged(nbOfRows);
    }

    @Override
    public void onColumnClick(String columnName) {
        super.onColumnClick(columnName);
    }

    @Override
    public void onGroupLinkChange(Object key) {
        super.onGroupLinkChange(key);
    }

    @Override
    public void onGroupLinkSecondChange(Object key) {
        super.onGroupLinkSecondChange(key);
    }

    @Override
    public void onDisplayModeChange(int displayMode) {
        super.onDisplayModeChange(displayMode);
    }

    @Override
    public void onRowsChanged(int nbOfRows) {
        super.onRowsChanged(nbOfRows);
    }

    @Override
    public void onSorterChanged(String columnName, int order) {
        super.onSorterChanged(columnName, order);
    }

    @Override
    public boolean onDeleteEvent(ActionEvent event) {
        return super.onDeleteEvent(event);
    }

    @Override
    public boolean onDeleteEvent(ActionEvent event, boolean force) {
        return super.onDeleteEvent(event, force);
    }

    @Override
    public boolean onMoveEvent(ActionEvent event) {
        return super.onMoveEvent(event);
    }

    @Override
    public boolean onActionEvent(ActionEvent event) {
        return super.onActionEvent(event);
    }

    @Override
    public boolean close() {
        return super.close();
    }

    @Override
    public boolean print(ActionEvent event) {
        return super.print(event);
    }

    @Override
    public boolean mustRefresh() {
        return super.mustRefresh();
    }

    @Override
    protected void refresh() {
        super.refresh();
    }

    @Override
    protected void delete(List<?> itemsToRemove) {
        super.delete(itemsToRemove);
    }

    @Override
    protected void move(List<Object> itemsToMove, boolean moveUp) {
        super.move(itemsToMove, moveUp);
    }

    @Override
    public boolean onNavigate(INavigateContext context) {
        return super.onNavigate(context);
    }

    @Override
    public void onRowsPerPageChanged(int nbRowsPerPage) {
        super.onRowsPerPageChanged(nbRowsPerPage);
    }

    @Override
    public CtlAbstractView.CtlViewBrowserEntry createBrowserEntry(CtlText name) {
        return super.createBrowserEntry(name);
    }

    @Override
    public void onRefresh() {
        super.onRefresh();
    }

    @Override
    public boolean save() {
        return super.save();
    }

    @Override
    public IViewModel getModel() {
        return super.getModel();
    }

    @Override
    public void setModel(IViewModel model) {
        super.setModel(model);
    }

    @Override
    public CtlAbstractView getView() {
        return super.getView();
    }

    @Override
    public CtlListView getListView() {
        return super.getListView();
    }

    @Override
    public INavigateContext getContext() {
        return super.getContext();
    }

    @Override
    public String getDeleteConfirmationMessage() {
        return super.getDeleteConfirmationMessage();
    }

    @Override
    public void setDeleteConfirmationMessage(String deleteConfirmationMessage) {
        super.setDeleteConfirmationMessage(deleteConfirmationMessage);
    }

    @Override
    public boolean isSelectable() {
        return super.isSelectable();
    }

    @Override
    public void setSelectable(boolean isSelectable) {
        super.setSelectable(isSelectable);
    }

    @Override
    public boolean isAllowImages() {
        return super.isAllowImages();
    }

    @Override
    public void setAllowImages(boolean allowImages) {
        super.setAllowImages(allowImages);
    }

    @Override
    public boolean isExportable() {
        return super.isExportable();
    }

    @Override
    public void setExportable(boolean isExportable) {
        super.setExportable(isExportable);
    }

    @Override
    public XMLSelector getParentSelector() {
        return super.getParentSelector();
    }

    @Override
    public void setParentSelector(XMLSelector parentSelector) {
        super.setParentSelector(parentSelector);
    }

    @Override
    public void clearActions() {
        super.clearActions();
    }

    @Override
    public List<NavigationObject> getActions() {
        return super.getActions();
    }

    @Override
    public void setActions(List<NavigationObject> actions) {
        super.setActions(actions);
    }

    @Override
    public void addAction(NavigationObject action) {
        super.addAction(action);
    }

    @Override
    public void removeAction(String name) {
        super.removeAction(name);
    }

    @Override
    public boolean displayImageZone() {
        return super.displayImageZone();
    }

    @Override
    public boolean displayPrimaryZone() {
        return super.displayPrimaryZone();
    }

    @Override
    public List<NavigationObject> getActions(int zone) {
        return super.getActions(zone);
    }

    @Override
    public List<NavigationObject> getActions(int zone, ViewModelItem item, int lineNumber) {
        return super.getActions(zone, item, lineNumber);
    }

    @Override
    public List<NavigationObject> getActions(int zone, ViewModelItem item, int lineNumber, boolean excludeDisabled) {
        return super.getActions(zone, item, lineNumber, excludeDisabled);
    }

    @Override
    protected CtlDiv getPrimaryActionsDiv(ViewModelItem viewModelItem, String objectName, Object key, int lineNumber) {
        return super.getPrimaryActionsDiv(viewModelItem, objectName, key, lineNumber);
    }

    @Override
    public Set<ChangeListener> getCheckboxListeners() {
        return super.getCheckboxListeners();
    }

    @Override
    public void addCheckboxListener(ChangeListener checkboxListener) {
        super.addCheckboxListener(checkboxListener);
    }

    @Override
    public Element getFilterFormElement() {
        return super.getFilterFormElement();
    }

    @Override
    public void setFilterFormElement(Element filterFormElement) {
        super.setFilterFormElement(filterFormElement);
    }

    @Override
    public boolean keyEquals(Object obj1, Object obj2) {
        return super.keyEquals(obj1, obj2);
    }

    @Override
    public boolean isInitialized() {
        return super.isInitialized();
    }

    @Override
    public void afterFetch() {
        super.afterFetch();
    }

    @Override
    public void beforeFetch() {
        super.beforeFetch();
    }

    @Override
    public void addChangeListener(ChangeListener listener) {
        super.addChangeListener(listener);
    }

    @Override
    public ChangeListener removeChangeListener(ChangeListener listener) {
        return super.removeChangeListener(listener);
    }

    @Override
    public void removeChangeListeners() {
        super.removeChangeListeners();
    }

    @Override
    public boolean hasChangeListeners() {
        return super.hasChangeListeners();
    }

    @Override
    protected void stateChanged() {
        super.stateChanged();
    }

    @Override
    public Navigator getNavigator() {
        return super.getNavigator();
    }

    @Override
    public RootNavigator getRootNavigator() {
        return super.getRootNavigator();
    }

    @Override
    protected Component createComponent(String className) {
        return super.createComponent(className);
    }

    @Override
    public String getStaticString(String entryID, String arg1) {
        return super.getStaticString(entryID, arg1);
    }

    @Override
    public String getStaticString(String entryID, String arg1, String arg2) {
        return super.getStaticString(entryID, arg1, arg2);
    }

    @Override
    public String getStaticString(String entryID, String arg1, String arg2, String arg3) {
        return super.getStaticString(entryID, arg1, arg2, arg3);
    }

    @Override
    public String getStaticString(String entryID, String arg1, String arg2, String arg3, String arg4) {
        return super.getStaticString(entryID, arg1, arg2, arg3, arg4);
    }

    @Override
    public String getStaticString(String entryID) {
        return super.getStaticString(entryID);
    }

    @Override
    public String getLanguage() {
        return super.getLanguage();
    }

    @Override
    public String getDynamicLabel(IDomainLocalizable localizable) {
        return super.getDynamicLabel(localizable);
    }

    @Override
    public String getDynamicDescription(IDomainLocalizable localizable) {
        return super.getDynamicDescription(localizable);
    }

    @Override
    public String evaluateExpressions(String formula) {
        return super.evaluateExpressions(formula);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    public String toString() {
        return super.toString();
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
    }
}
