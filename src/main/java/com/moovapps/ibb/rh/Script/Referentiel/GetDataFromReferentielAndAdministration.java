package com.moovapps.ibb.rh.Script.Referentiel;

import com.axemble.vdoc.sdk.Modules;
import com.axemble.vdoc.sdk.interfaces.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


public class GetDataFromReferentielAndAdministration {

    protected IStorageResource getSocieteByName(String societeName) {
        try {
            IContext context = Modules.getDirectoryModule().getSysadminContext();
            IOrganization organization = Modules.getDirectoryModule().getOrganization(context, "DefaultOrganization");
            IProject project = Modules.getProjectModule().getProject(context, "REFERENTIELCOMMUN", organization);
            ICatalog catalog = Modules.getWorkflowModule().getCatalog(context, "REFERENTIEL", 4, project);
            IViewController controller = Modules.getWorkflowModule().getViewController(context, IResource.class);
            IResourceDefinition definition = Modules.getWorkflowModule().getResourceDefinition(context, catalog, "Societe");
            controller.addEqualsConstraint("sys_Title", societeName);
            Collection<IStorageResource> societe = controller.evaluate(definition);
            if (!societe.isEmpty()) {
                return societe.iterator().next();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    protected IStorageResource getDirectionByName(String DirectionName) {
        try {
            IContext context = Modules.getDirectoryModule().getSysadminContext();
            IOrganization organization = Modules.getDirectoryModule().getOrganization(context, "DefaultOrganization");
            IProject project = Modules.getProjectModule().getProject(context, "REFERENTIELCOMMUN", organization);
            ICatalog catalog = Modules.getWorkflowModule().getCatalog(context, "REFERENTIEL", 4, project);
            IViewController controller = Modules.getWorkflowModule().getViewController(context, IResource.class);
            IResourceDefinition definition = Modules.getWorkflowModule().getResourceDefinition(context, catalog, "Direction");
            controller.addEqualsConstraint("sys_Title", DirectionName);
            Collection<IStorageResource> societe = controller.evaluate(definition);
            if (!societe.isEmpty()) {
                return societe.iterator().next();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    protected IStorageResource getDepartementByName(String departementName) {
        try {
            IContext context = Modules.getDirectoryModule().getSysadminContext();
            IOrganization organization = Modules.getDirectoryModule().getOrganization(context, "DefaultOrganization");
            IProject project = Modules.getProjectModule().getProject(context, "REFERENTIELCOMMUN", organization);
            ICatalog catalog = Modules.getWorkflowModule().getCatalog(context, "REFERENTIEL", 4, project);
            IViewController controller = Modules.getWorkflowModule().getViewController(context, IResource.class);
            IResourceDefinition definition = Modules.getWorkflowModule().getResourceDefinition(context, catalog, "departementcommun");
            controller.addEqualsConstraint("sys_Title", departementName);
            Collection<IStorageResource> societe = controller.evaluate(definition);
            if (!societe.isEmpty()) {
                return societe.iterator().next();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    protected IStorageResource getFontionByName(String fonctionName) {
        try {
            IContext context = Modules.getDirectoryModule().getSysadminContext();
            IOrganization organization = Modules.getDirectoryModule().getOrganization(context, "DefaultOrganization");
            IProject project = Modules.getProjectModule().getProject(context, "REFERENTIELCOMMUN", organization);
            ICatalog catalog = Modules.getWorkflowModule().getCatalog(context, "REFERENTIEL", 4, project);
            IViewController controller = Modules.getWorkflowModule().getViewController(context, IResource.class);
            IResourceDefinition definition = Modules.getWorkflowModule().getResourceDefinition(context, catalog, "fonctions");
            controller.addEqualsConstraint("sys_Title", fonctionName);
            Collection<IStorageResource> societe = controller.evaluate(definition);
            if (!societe.isEmpty()) {
                return societe.iterator().next();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    protected IStorageResource getStorageByField(String storageDefinition,String fieldName,String filedValue) {
        try {
            IContext context = Modules.getDirectoryModule().getSysadminContext();
            IOrganization organization = Modules.getDirectoryModule().getOrganization(context, "DefaultOrganization");
            IProject project = Modules.getProjectModule().getProject(context, "REFERENTIELCOMMUN", organization);
            ICatalog catalog = Modules.getWorkflowModule().getCatalog(context, "REFERENTIEL", 4, project);
            IViewController controller = Modules.getWorkflowModule().getViewController(context, IResource.class);
            IResourceDefinition definition = Modules.getWorkflowModule().getResourceDefinition(context, catalog, storageDefinition);
            controller.addEqualsConstraint(fieldName, filedValue);
            Collection<IStorageResource> societe = controller.evaluate(definition);
            if (!societe.isEmpty()) {
                return societe.iterator().next();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    protected IStorageResource getStorageByFieldAndFilter(String storageDefinition, String fieldName, String filedValue , HashMap<String,Object> filter) {
        try {
            IContext context = Modules.getDirectoryModule().getSysadminContext();
            IOrganization organization = Modules.getDirectoryModule().getOrganization(context, "DefaultOrganization");
            IProject project = Modules.getProjectModule().getProject(context, "REFERENTIELCOMMUN", organization);
            ICatalog catalog = Modules.getWorkflowModule().getCatalog(context, "REFERENTIEL", 4, project);
            IViewController controller = Modules.getWorkflowModule().getViewController(context, IResource.class);
            IResourceDefinition definition = Modules.getWorkflowModule().getResourceDefinition(context, catalog, storageDefinition);
            controller.addEqualsConstraint(fieldName, filedValue);
            for (Map.Entry<String, Object> entry : filter.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                controller.addEqualsConstraint(key, value);
            }
            Collection<IStorageResource> societe = controller.evaluate(definition);
            if (!societe.isEmpty()) {
                return societe.iterator().next();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    protected IStorageResource getStorageByFieldAndFilter(String projet , String catalogTEXT,String storageDefinition, String fieldName, String filedValue , HashMap<String,Object> filter) {
        try {
            IContext context = Modules.getDirectoryModule().getSysadminContext();
            IOrganization organization = Modules.getDirectoryModule().getOrganization(context, "DefaultOrganization");
            IProject project = Modules.getProjectModule().getProject(context, projet, organization);
            ICatalog catalog = Modules.getWorkflowModule().getCatalog(context, catalogTEXT, 4, project);
            IViewController controller = Modules.getWorkflowModule().getViewController(context, IResource.class);
            IResourceDefinition definition = Modules.getWorkflowModule().getResourceDefinition(context, catalog, storageDefinition);
            controller.addEqualsConstraint(fieldName, filedValue);
            for (Map.Entry<String, Object> entry : filter.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                controller.addEqualsConstraint(key, value);
            }
            Collection<IStorageResource> societe = controller.evaluate(definition);
            if (!societe.isEmpty()) {
                return societe.iterator().next();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    protected IStorageResource getCategorieByName(String categorieName) {
        try {
            IContext context = Modules.getDirectoryModule().getSysadminContext();
            IOrganization organization = Modules.getDirectoryModule().getOrganization(context, "DefaultOrganization");
            IProject project = Modules.getProjectModule().getProject(context, "REFERENTIELCOMMUN", organization);
            ICatalog catalog = Modules.getWorkflowModule().getCatalog(context, "REFERENTIEL", 4, project);
            IViewController controller = Modules.getWorkflowModule().getViewController(context, IResource.class);
            IResourceDefinition definition = Modules.getWorkflowModule().getResourceDefinition(context, catalog, "Categorie");
            controller.addEqualsConstraint("sys_Title", categorieName);
            Collection<IStorageResource> societe = controller.evaluate(definition);
            if (!societe.isEmpty()) {
                return societe.iterator().next();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    protected IStorageResource getCollaborateurByMatricule(String field , String name) {
        try {
            IContext context = Modules.getDirectoryModule().getSysadminContext();
            IOrganization organization = Modules.getDirectoryModule().getOrganization(context, "DefaultOrganization");
            IProject project = Modules.getProjectModule().getProject(context, "REFERENTIELCOMMUN", organization);
            ICatalog catalog = Modules.getWorkflowModule().getCatalog(context, "REFERENTIEL", 4, project);
            IViewController controller = Modules.getWorkflowModule().getViewController(context, IResource.class);
            IResourceDefinition definition = Modules.getWorkflowModule().getResourceDefinition(context, catalog, "FicheCollaborateur");
            controller.addEqualsConstraint(field, name);
           
            Collection<IStorageResource> collaberateurs = controller.evaluate(definition);
            if (!collaberateurs.isEmpty()) {
                return (IStorageResource) collaberateurs.iterator().next();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    protected IStorageResource getCollaborateurByCIN(String cin) {
        try {
            IContext context = Modules.getDirectoryModule().getSysadminContext();
            IOrganization organization = Modules.getDirectoryModule().getOrganization(context, "DefaultOrganization");
            IProject project = Modules.getProjectModule().getProject(context, "REFERENTIELCOMMUN", organization);
            ICatalog catalog = Modules.getWorkflowModule().getCatalog(context, "REFERENTIEL", 4, project);
            IViewController controller = Modules.getWorkflowModule().getViewController(context, IResource.class);
            IResourceDefinition definition = Modules.getWorkflowModule().getResourceDefinition(context, catalog, "FicheCollaborateur");
            controller.addEqualsConstraint("CIN", cin);
            //controller.addEqualsConstraint("Societe", societe);
            Collection<IStorageResource> collaberateurs = controller.evaluate(definition);
            if (!collaberateurs.isEmpty()) {
                return (IStorageResource) collaberateurs.iterator().next();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }



    protected IGroup getGroupByName(String groupeName, IOrganization organization) {
        try {
            if(groupeName.equals("News readers")){
                groupeName = "NEWS_ANIMATION";
            }else if(groupeName.equals("Workplace FAQ administrators")){
                groupeName = "WP_ADMIN_MIDDLE_OFFICE_FAQ";
            }else if(groupeName.equals("salarié")){
                groupeName = "Salarie";
            }else if(groupeName.equals("RH")){
                groupeName = "RH";
            }else if(groupeName.equals("Manager")){
                groupeName = "Manager";
            }else if(groupeName.equals("admin")){
                groupeName = "BPO";
            }
            IContext context = Modules.getDirectoryModule().getSysadminContext();
            IGroup group = Modules.getDirectoryModule().getGroup(context, organization, organization.getName() + groupeName);
            if (group == null) {
                group = Modules.getDirectoryModule().getGroup(context, organization, groupeName);
            }
            return group;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    protected IOrganization getOrganization(String nameOrganization) {
        try {
            IContext context = Modules.getDirectoryModule().getSysadminContext();
            IOrganization organization = Modules.getDirectoryModule().getOrganization(context, nameOrganization);
            return organization;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    protected IUser getUserByNameAndOrganization(String nameUser, IOrganization organization) {
        //getUserTest(nameUser);
        IContext context = Modules.getDirectoryModule().getSysadminContext();
        Collection<IUser> users = (Collection<IUser>) Modules.getDirectoryModule().getUsers(null, organization);
        for (IUser user : users) {
            if (user.getFullName().trim().equals(nameUser.trim())) {
                return user;
            }
        }
        return null;
    }

    /*public IUser getUserTest(String FullName) {

        HashMap<String,Object> t = new HashMap<String,Object>(){{
            put("FullName",FullName);
        }};

        User u = VDocManagers.getDirectoryManager().getUserByAttributes(t);

        return u;
    }*/
}
