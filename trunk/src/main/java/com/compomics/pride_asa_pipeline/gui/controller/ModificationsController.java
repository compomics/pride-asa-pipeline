/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pride_asa_pipeline.gui.controller;

import com.compomics.pride_asa_pipeline.config.PropertiesConfigurationHolder;
import com.compomics.pride_asa_pipeline.gui.view.ModificationsPanel;
import com.compomics.pride_asa_pipeline.model.AminoAcid;
import com.compomics.pride_asa_pipeline.model.Modification;
import com.compomics.pride_asa_pipeline.service.ModificationService;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.swing.ListModel;
import org.jdesktop.beansbinding.*;
import org.jdesktop.observablecollections.ObservableCollections;
import org.jdesktop.observablecollections.ObservableList;
import org.jdesktop.swingbinding.JTableBinding;
import org.jdesktop.swingbinding.JTableBinding.ColumnBinding;
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

        //fill location combobox
        for (Modification.Location location : Modification.Location.values()) {
            modificationsPanel.getModLocationComboBox().addItem(location);
        }

        //init bindings

        //table binding
        modificationsBindingList = ObservableCollections.observableList(getModificationsAsList(
                modificationService.loadPipelineModifications(PropertiesConfigurationHolder.getInstance().getString("modification.pipeline_modifications_file_name"))));
        bindingGroup = new BindingGroup();
        JTableBinding modificationsTableBinding = SwingBindings.createJTableBinding(AutoBinding.UpdateStrategy.READ, modificationsBindingList, modificationsPanel.getModifcationsTable());

        //Add column bindings
        ColumnBinding columnBinding = modificationsTableBinding.addColumnBinding(ELProperty.create("${name}"));
        columnBinding.setColumnName("Name");
        columnBinding.setColumnClass(String.class);

        columnBinding = modificationsTableBinding.addColumnBinding(ELProperty.create("${modificationAccession}"));
        columnBinding.setColumnName("Accession");
        columnBinding.setColumnClass(String.class);

        columnBinding = modificationsTableBinding.addColumnBinding(ELProperty.create("${massShift}"));
        columnBinding.setColumnName("Mass shift");
        columnBinding.setColumnClass(String.class);

        columnBinding = modificationsTableBinding.addColumnBinding(ELProperty.create("${location}"));
        columnBinding.setColumnName("Location");
        columnBinding.setColumnClass(Modification.Location.class);

        bindingGroup.addBinding(modificationsTableBinding);

        //selected modication in table bindings
        Binding nameBinding = Bindings.createAutoBinding(AutoBinding.UpdateStrategy.READ_WRITE, modificationsPanel.getModifcationsTable(), ELProperty.create("${selectedElement.name}"), modificationsPanel.getModNameTextField(), BeanProperty.create("text"));
        bindingGroup.addBinding(nameBinding);

        Binding accessionBinding = Bindings.createAutoBinding(AutoBinding.UpdateStrategy.READ_WRITE, modificationsPanel.getModifcationsTable(), ELProperty.create("${selectedElement.modificationAccession}"), modificationsPanel.getModAccessionTextField(), BeanProperty.create("text"));
        bindingGroup.addBinding(accessionBinding);

        Binding massShiftBinding = Bindings.createAutoBinding(AutoBinding.UpdateStrategy.READ_WRITE, modificationsPanel.getModifcationsTable(), ELProperty.create("${selectedElement.massShift}"), modificationsPanel.getModMassShiftTextField(), BeanProperty.create("text"));
        bindingGroup.addBinding(massShiftBinding);

        Binding locationBinding = Bindings.createAutoBinding(AutoBinding.UpdateStrategy.READ_WRITE, modificationsPanel.getModifcationsTable(), ELProperty.create("${selectedElement.location}"), modificationsPanel.getModLocationComboBox(), BeanProperty.create("selectedItem"));
        bindingGroup.addBinding(locationBinding);

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
