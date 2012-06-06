/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pride_asa_pipeline.gui.controller;

import com.compomics.pride_asa_pipeline.config.PropertiesConfigurationHolder;
import com.compomics.pride_asa_pipeline.gui.view.ModificationsPanel;
import com.compomics.pride_asa_pipeline.model.Modification;
import com.compomics.pride_asa_pipeline.service.ModificationService;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.BindingGroup;
import org.jdesktop.observablecollections.ObservableCollections;
import org.jdesktop.observablecollections.ObservableList;
import org.jdesktop.swingbinding.JTableBinding;
import org.jdesktop.swingbinding.SwingBindings;

/**
 *
 * @author Niels Hulstaert
 */
public class ModificationsController {

    //model
    private BindingGroup bindingGroup;
    private ObservableList<Modification> modificationsBindingList;
    //view
    private ModificationsPanel modificationsPanel;
    //parent controller
    private MainController mainController;
    //services
    private ModificationService modificationService;

    public ModificationsController() {
    }

    public ModificationsPanel getModificationsPanel() {
        return modificationsPanel;
    }        

    public ModificationService getModificationService() {
        return modificationService;
    }

    public void setModificationService(ModificationService modificationService) {
        this.modificationService = modificationService;
    }

    public void init() {
        modificationsPanel = new ModificationsPanel();

        //init binding
        modificationsBindingList = ObservableCollections.observableList(getModificationsAsList(
            modificationService.loadPipelineModifications(PropertiesConfigurationHolder.getInstance().getString("modification.pipeline_modifications_file_name"))));
        bindingGroup = new BindingGroup();
        JTableBinding modificationTableBinding = SwingBindings.createJTableBinding(AutoBinding.UpdateStrategy.READ_WRITE, modificationsBindingList, modificationsPanel.getModifcationTable());
        bindingGroup.addBinding(modificationTableBinding);
        bindingGroup.bind();        
    }

    private List<Modification> getModificationsAsList(Set<Modification> modificationSet) {
        List<Modification> modifications = new ArrayList<Modification>();
        for (Modification modification : modificationSet) {
            modifications.add(modification);
        }

        return modifications;
    }
}
