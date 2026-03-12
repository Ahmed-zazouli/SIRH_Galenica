package com.Providers;

import com.axemble.commons.filters.FilterGroup;
import com.axemble.commons.filters.Operator;
import com.axemble.commons.filters.PropertyFilter;
import com.axemble.vdoc.core.helpers.ProtocolURIHelper;
import com.axemble.vdoc.sdk.interfaces.IProperty;
import com.axemble.vdoc.sdk.interfaces.runtime.INavigateContext;
import com.axemble.vdoc.sdk.interfaces.runtime.IProvider;
import com.axemble.vdoc.storage.ui.core.providers.views.ResourceViewProvider;
import com.axemble.vdp.resource.classes.ITableCreation;
import com.axemble.vdp.resource.domain.ResourceDefinition;
import com.axemble.vdp.ui.core.document.CoreDocument;
import com.axemble.vdp.ui.core.document.FormContext;
import com.axemble.vdp.ui.core.document.fields.ICoreField;
import com.axemble.vdp.ui.framework.composites.base.CtlAbstractResource;
import com.axemble.vdp.ui.framework.composites.base.CtlAbstractView;
import com.axemble.vdp.ui.framework.composites.base.models.views.XMLViewModel;
import com.axemble.vdp.ui.framework.foundation.parts.LightBox;
import com.axemble.vdp.ui.framework.foundation.parts.Part;
import com.axemble.vdp.ui.framework.widgets.CtlNavigationButton;
import com.axemble.vdp.view.transformer.IViewTransformer;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Collection;

public class CustomSetterProvider extends ResourceViewProvider {
    public CustomSetterProvider(INavigateContext context, CtlAbstractView view) {
        super(context, view);
    }

    @Override
    public void loadModel()
    {
        String from = "FROM";
        String TO = "TO";
        if ( viewDefinition != null && StringUtils.isNotEmpty( this.xmlViewElement.getAttribute( CONTEXT_PARAMETER_COLUMN ) ) && this.context.getParameterObject( IProvider.CURRENT_DOCUMENT ) != null )
        {
            String filterColumn = this.xmlViewElement.getAttribute( CONTEXT_PARAMETER_COLUMN );
            CoreDocument coreDocument = (CoreDocument)this.context.getParameterObject( IProvider.CURRENT_DOCUMENT );
            if ( navigationPartName == null ) // Le pb est qu'en delete on reste sur la m�me part => On le r�cup�re donc uniquement � la premi�re init
            {
                Part lightbox = getRootNavigator().getPartByName( LightBox.PART_NAME );
                if ( lightbox != null )
                    navigationPartName = lightbox.getName();
                else navigationPartName = getNavigator().getParentNavigator().getPart().getName();
            }

            try
            {
                // Load model
                super.loadModel();

                IViewTransformer viewTransformer = null;
                if ( this.model instanceof XMLViewModel)
                    viewTransformer = ( (XMLViewModel)this.model ).getViewTransformer();
                ResourceDefinition rd = viewTransformer.getCurrentResourceDefinition();
                ITableCreation iTableCreation = rd;
                if ( iTableCreation == null )
                    iTableCreation = viewTransformer.getCurrentCatalog();
                boolean isProtocolURI = iTableCreation.getProperty( filterColumn ) != null && iTableCreation.getProperty( filterColumn ).isProtocolUri();

                // On ajoute la contrainte sur la colonne
                Object value = null;
                if ( coreDocument.getResource() != null && coreDocument.getResource().isPersistent() )
                    value = isProtocolURI ? ProtocolURIHelper.getResourceUri( coreDocument.getResource() ) : coreDocument.getResource().getId().toLong();
                FilterGroup fg = viewTransformer.getFilterGroup();
                fg.getFilters().remove(fg.getFilters().size() - 1);
//                fg.addFilter( new PropertyFilter( "IDCommande", Operator.EQUALS, coreDocument.getResource().getValue("IDCommande") ) );
                fg.addFilter( new PropertyFilter( filterColumn, Operator.EQUALS, value ) );
                getView().getFilterFormProvider().initFilters( fg ); // Un peu de la bidouille. A voir si on peut faire mieux apr�s

                // Modify Create button
                CtlNavigationButton createButton = (CtlNavigationButton)view.getButton( "create" );

                // Handle Creation mode (no create button, no doc because id does not exist)
                viewTransformer.setDisabled( value == null );
                if ( createButton != null )
                    createButton.setHidden( value == null );

                // On pose la value
                if ( createButton != null && value != null && rd != null ) // On ne g�re la cr�ation que pour le data universe pour l'instant
                {
                    CoreDocument srcDocument = new CoreDocument();
                    srcDocument.setContext( FormContext.IRESOURCEDEFINITION, rd );
                    ICoreField filterField = srcDocument.getFieldByName( filterColumn );
                    if ( filterField.getProperty() != null && filterField.getProperty().getType() == IProperty.IType.COLLECTION )
                    { // Multiple value field
                        Collection<Object> values = new ArrayList<Object>();
                        values.add( coreDocument.getResource() );
                        filterField.setValue( values );
                    }
                    else filterField.setValue( coreDocument.getResource() );

                    String[] froms = this.xmlViewElement.getAttribute( "from" ).split(",");
                    String[] tos = this.xmlViewElement.getAttribute( "to" ).split(",");
                    for (int i = 0; i < froms.length; i++) {
                        ICoreField fieldToSet = srcDocument.getFieldByName( tos[i] );
                        if ( fieldToSet.getProperty() != null && fieldToSet.getProperty().getType() == IProperty.IType.COLLECTION )
                        { // Multiple value field
                            Collection<Object> values = new ArrayList<Object>();
                            values.add( coreDocument.getResource().getValue(froms[i]) );
                            fieldToSet.setValue( values );
                        }
                        else fieldToSet.setValue( coreDocument.getResource().getValue(froms[i]) );
                    }

                    createButton.setObjectName( "" ); // Remove object name
                    createButton.setParameter( "resource_template", rd.getId().toString() );
                    createButton.setParameter( CtlAbstractResource.PARAM_SRCDOCCONTROLLER, srcDocument );
                }
            }
            catch( Exception e )
            {
                getNavigator().processErrors( e );
            }

        }
        else super.loadModel(); // Est-ce que ce cas arrive vraiment ?
    }
}
