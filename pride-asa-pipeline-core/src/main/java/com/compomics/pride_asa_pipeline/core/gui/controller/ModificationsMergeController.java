/* 
 * Copyright 2018 compomics.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.compomics.pride_asa_pipeline.core.gui.controller;

import com.compomics.pride_asa_pipeline.core.gui.view.ModificationsMergeDialog;
import com.compomics.pride_asa_pipeline.core.model.ModificationHolder;
import com.compomics.pride_asa_pipeline.model.Modification;
import com.compomics.pride_asa_pipeline.core.util.GuiUtils;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Binding;
import org.jdesktop.beansbinding.BindingGroup;
import org.jdesktop.beansbinding.Bindings;
import org.jdesktop.beansbinding.ELProperty;
import org.jdesktop.observablecollections.ObservableCollections;
import org.jdesktop.observablecollections.ObservableList;
import org.jdesktop.swingbinding.JListBinding;
import org.jdesktop.swingbinding.SwingBindings;

/**
 *
 * @author Niels Hulstaert
 */
public class ModificationsMergeController extends WindowAdapter {

    //model
    private BindingGroup bindingGroup;
    private ObservableList<Modification> prideModificationsBindingList;
    private ObservableList<Modification> pipelineModificationsBindingList;
    /**
     * Holder for the pride modifications that have already been added to the
     * pipeline modifications.
     */
    private Set<Modification> addedPrideModifications;
    //view
    private ModificationsMergeDialog modificationsMergeDialog;
    //parent controller
    private ExperimentSelectionController experimentSelectionController;

    public ExperimentSelectionController getExperimentSelectionController() {
        return experimentSelectionController;
    }

    public void setExperimentSelectionController(ExperimentSelectionController experimentSelectionController) {
        this.experimentSelectionController = experimentSelectionController;
    }

    public void showDialog(ModificationHolder modificationHolder, Set<Modification> prideModifications, Set<Modification> conflictingModifications) {        
        addedPrideModifications.clear();
                
        //add the non conflicting pride modifications to the pipeline modifications
        for(Modification prideModification : prideModifications){
            if(!conflictingModifications.contains(prideModification)){
                addedPrideModifications.add(prideModification);
            }            
        }

        //update observable collections
        prideModificationsBindingList.clear();
        prideModificationsBindingList.addAll(conflictingModifications);
        //sort modifications
        Collections.sort(prideModificationsBindingList);

        pipelineModificationsBindingList.clear();        
        pipelineModificationsBindingList.addAll(modificationHolder.getAllModifications());
        pipelineModificationsBindingList.addAll(addedPrideModifications);
        //sort modifications
        Collections.sort(pipelineModificationsBindingList);

        GuiUtils.centerDialogOnFrame(experimentSelectionController.getMainController().getMainFrame(), modificationsMergeDialog);
        modificationsMergeDialog.setVisible(true);
    }

    public void init() {
        modificationsMergeDialog = new ModificationsMergeDialog(experimentSelectionController.getMainController().getMainFrame());
        modificationsMergeDialog.addWindowListener(this);

        addedPrideModifications = new HashSet<>();

        //set list cell renderer
        modificationsMergeDialog.getPipelineModificationsList().setCellRenderer(new PipelineModificationsRenderer());

        //init bindings
        bindingGroup = new BindingGroup();

        //modifications list bindings
        prideModificationsBindingList = ObservableCollections.observableList(new ArrayList<Modification>());
        JListBinding prideModificationsBinding = SwingBindings.createJListBinding(AutoBinding.UpdateStrategy.READ_WRITE, prideModificationsBindingList, modificationsMergeDialog.getPrideModificationsList());
        bindingGroup.addBinding(prideModificationsBinding);

        pipelineModificationsBindingList = ObservableCollections.observableList(new ArrayList<Modification>());
        JListBinding pipelineModificationsBinding = SwingBindings.createJListBinding(AutoBinding.UpdateStrategy.READ_WRITE, pipelineModificationsBindingList, modificationsMergeDialog.getPipelineModificationsList());
        bindingGroup.addBinding(pipelineModificationsBinding);

        //pride modification binding        
        Binding prideModAccessionBinding = Bindings.createAutoBinding(AutoBinding.UpdateStrategy.READ, modificationsMergeDialog.getPrideModificationsList(), ELProperty.create("${selectedElement.accession}"), modificationsMergeDialog.getPrideModAccessionTextField(), BeanProperty.create("text"), "prideModAccession");
        bindingGroup.addBinding(prideModAccessionBinding);
        Binding prideModAccessionValueBinding = Bindings.createAutoBinding(AutoBinding.UpdateStrategy.READ, modificationsMergeDialog.getPrideModificationsList(), ELProperty.create("${selectedElement.accessionValue}"), modificationsMergeDialog.getPrideModAccessionValueTextField(), BeanProperty.create("text"), "prideModAccessionValue");
        bindingGroup.addBinding(prideModAccessionValueBinding);
        Binding prideModMonoMassShiftBinding = Bindings.createAutoBinding(AutoBinding.UpdateStrategy.READ, modificationsMergeDialog.getPrideModificationsList(), ELProperty.create("${selectedElement.monoIsotopicMassShift}"), modificationsMergeDialog.getPrideModMonoMassShiftTextField(), BeanProperty.create("text"), "prideModMonoMassShift");
        bindingGroup.addBinding(prideModMonoMassShiftBinding);
        Binding prideModAverageMassShiftBinding = Bindings.createAutoBinding(AutoBinding.UpdateStrategy.READ, modificationsMergeDialog.getPrideModificationsList(), ELProperty.create("${selectedElement.averageMassShift}"), modificationsMergeDialog.getPrideModAverageModMassShiftTextField(), BeanProperty.create("text"), "prideModAverageMassShift");
        bindingGroup.addBinding(prideModAverageMassShiftBinding);

        //pipeline modification binding        
        Binding modAccessionBinding = Bindings.createAutoBinding(AutoBinding.UpdateStrategy.READ, modificationsMergeDialog.getPipelineModificationsList(), ELProperty.create("${selectedElement.accession}"), modificationsMergeDialog.getModAccessionTextField(), BeanProperty.create("text"), "modAccession");
        bindingGroup.addBinding(modAccessionBinding);
        Binding modAccessionValueBinding = Bindings.createAutoBinding(AutoBinding.UpdateStrategy.READ, modificationsMergeDialog.getPipelineModificationsList(), ELProperty.create("${selectedElement.accessionValue}"), modificationsMergeDialog.getModAccessionValueTextField(), BeanProperty.create("text"), "modAccessionValue");
        bindingGroup.addBinding(modAccessionValueBinding);
        Binding modMonoMassShiftBinding = Bindings.createAutoBinding(AutoBinding.UpdateStrategy.READ, modificationsMergeDialog.getPipelineModificationsList(), ELProperty.create("${selectedElement.monoIsotopicMassShift}"), modificationsMergeDialog.getModMonoMassShiftTextField(), BeanProperty.create("text"), "modMonoMassShift");
        bindingGroup.addBinding(modMonoMassShiftBinding);
        Binding modAverageMassShiftBinding = Bindings.createAutoBinding(AutoBinding.UpdateStrategy.READ, modificationsMergeDialog.getPipelineModificationsList(), ELProperty.create("${selectedElement.averageMassShift}"), modificationsMergeDialog.getModAverageModMassShiftTextField(), BeanProperty.create("text"), "modAverageMassShift");
        bindingGroup.addBinding(modAverageMassShiftBinding);

        bindingGroup.bind();

        //add listeners
        modificationsMergeDialog.getAddPrideModificationButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedIndex = modificationsMergeDialog.getPrideModificationsList().getSelectedIndex();
                if (selectedIndex != -1) {
                    Modification selectedModification = prideModificationsBindingList.get(selectedIndex);
                    if (!pipelineModificationsBindingList.contains(selectedModification)) {
                        addedPrideModifications.add(selectedModification);
                        pipelineModificationsBindingList.add(selectedModification);
                        Collections.sort(pipelineModificationsBindingList);
                        prideModificationsBindingList.remove(selectedModification);
                    }
                }
            }
        });

        modificationsMergeDialog.getRemovePrideModificationButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedIndex = modificationsMergeDialog.getPipelineModificationsList().getSelectedIndex();
                if (selectedIndex != -1) {
                    Modification selectedModification = pipelineModificationsBindingList.get(selectedIndex);
                    if (addedPrideModifications.contains(selectedModification)) {
                        addedPrideModifications.remove(selectedModification);
                        prideModificationsBindingList.add(selectedModification);
                        Collections.sort(prideModificationsBindingList);
                        pipelineModificationsBindingList.remove(selectedModification);
                    }
                }
            }
        });

        modificationsMergeDialog.getCancelButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                modificationsMergeDialog.setVisible(Boolean.FALSE);
                experimentSelectionController.onAnnotationCanceled();
            }
        });

        modificationsMergeDialog.getProceedButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //add "added" pride modifications to the ModificationHolder
                experimentSelectionController.getMainController().getCurrentSpectrumAnnotator().getModificationHolder().addModifications(addedPrideModifications);                
                
                modificationsMergeDialog.setVisible(Boolean.FALSE);
                experimentSelectionController.onModificationsLoaded();
            }
        });
    }

    @Override
    public void windowClosed(WindowEvent e) {
        super.windowClosed(e);
        experimentSelectionController.onAnnotationCanceled();
    }

    private class PipelineModificationsRenderer extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            Modification modification = (Modification) value;
            if (addedPrideModifications.contains(modification)) {
                setText(modification.getName() + " (pride)");
            } else {
                setText(modification.getName());
            }
            return this;
        }
    }
}
