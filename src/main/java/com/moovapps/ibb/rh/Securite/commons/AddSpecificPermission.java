package com.moovapps.ibb.rh.Securite.commons;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.*;

import java.util.ArrayList;

public class AddSpecificPermission extends BaseDocumentExtension {

    public ISecurityController getSecuriteController(){
        try {
            return getWorkflowModule().getSecurityController(getWorkflowInstance());
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
    public ISecurityController getSecuriteController(IResource instance){
        if(instance == null){
            return null;
        }
        try {
            return getWorkflowModule().getSecurityController((IWorkflowInstance) instance);
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }



    @Override
    public boolean onBeforeLoad() {
      /*  ISecurityController securityController = getSecuriteController();
        breakInheritance(securityController);
        addManagementGroupes(securityController);
        addSpecificPermissions(securityController);*/
        return super.onBeforeLoad();
    }

    @Override
    public boolean onAfterSave() {
        try {
            ISecurityController securityController = getSecuriteController();
            breakInheritance(securityController);
            addManagementGroupes(securityController);
            addSpecificPermissions(getWorkflowInstance(),securityController);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return super.onAfterSave();
    }


    public void buildPermission(IResource instance){
        ISecurityController securityController = getSecuriteController(instance);
        breakInheritance(securityController);
        addManagementGroupes(securityController);
        addSpecificPermissions(instance,securityController);
    }

    public void addSpecificPermissions(IResource instance ,ISecurityController securityController) {
    }

    public void addManagementGroupes(ISecurityController securityController){
        try {
            IContext context = getWorkflowModule().getSysadminContext();
            IOrganization BPOGroupesOrganization = getDirectoryModule().getOrganization(context, "BPOGroupes");
            IGroup adminGroupe = getDirectoryModule().getGroup(context, BPOGroupesOrganization,"BPO");
            IGroup DRHGroupe = getDirectoryModule().getGroup(context, BPOGroupesOrganization,"DRH");
            IGroup RHGroupe = getDirectoryModule().getGroup(context, BPOGroupesOrganization,"RH");
           // IGroup ResponsableDevGroupe = getDirectoryModule().getGroup(context, BPOGroupesOrganization,"ResponsableSDEV");

            addPermission(securityController, adminGroupe, "read");
            addPermission(securityController, DRHGroupe, "read");
            addPermission(securityController, RHGroupe, "read");
            //addPermission(securityController, ResponsableDevGroupe, "read");

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void breakInheritance(ISecurityController securityController){
        securityController.breakInheritance(1, new Object[] { null, "write" });
    }

    public void addPermission(ISecurityController securityController, IUser user, String readWriteGrant){
        securityController.addPermission(user, new Object[] { null, readWriteGrant });
    }

    public void addPermission(ISecurityController securityController, IGroup group, String readWriteGrant){
        securityController.addPermission(group, new Object[] { null, readWriteGrant });
    }

    public void addPermission(ISecurityController securityController, ArrayList<IUser> user, String readWriteGrant){
        securityController.addPermission(user, new Object[] { null, readWriteGrant });
    }
}
