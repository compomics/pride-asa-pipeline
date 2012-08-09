/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pride_asa_pipeline.gui.controller;

import com.compomics.pride_asa_pipeline.gui.view.IdentificationsPanel;
import com.compomics.pride_asa_pipeline.gui.wrapper.IdentificationGuiWrapper;
import com.compomics.pride_asa_pipeline.model.AASequenceMassUnknownException;
import com.compomics.pride_asa_pipeline.model.AminoAcidSequence;
import com.compomics.pride_asa_pipeline.model.ModificationFacade;
import com.compomics.pride_asa_pipeline.model.ModifiedPeptide;
import com.compomics.pride_asa_pipeline.model.Peptide;
import com.compomics.pride_asa_pipeline.service.SpectrumPanelService;
import com.compomics.util.gui.spectrum.SpectrumPanel;
import com.google.common.base.Joiner;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import org.apache.log4j.Logger;
import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.BindingGroup;
import org.jdesktop.beansbinding.ELProperty;
import org.jdesktop.observablecollections.ObservableCollections;
import org.jdesktop.observablecollections.ObservableList;
import org.jdesktop.swingbinding.JTableBinding;
import org.jdesktop.swingbinding.JTableBinding.ColumnBinding;
import org.jdesktop.swingbinding.SwingBindings;

/**
 *
 * @author niels
 */
public class PipelineResultController {

    private static final Logger LOGGER = Logger.getLogger(PipelineResultController.class);
    private static final Integer NUMBER_OF_DECIMALS = 4;
    //model
    private BindingGroup bindingGroup;
    private ObservableList<IdentificationGuiWrapper> identificationGuiWrappersBindingList;
    //view
    private IdentificationsPanel identificationsPanel;
    //parent controller
    private MainController mainController;
    //services
    private SpectrumPanelService spectrumPanelService;

    public MainController getMainController() {
        return mainController;
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public IdentificationsPanel getPipelineResultPanel() {
        return identificationsPanel;
    }

    public SpectrumPanelService getSpectrumPanelService() {
        return spectrumPanelService;
    }

    public void setSpectrumPanelService(SpectrumPanelService spectrumPanelService) {
        this.spectrumPanelService = spectrumPanelService;
    }

    public void updateIdentifications() {
        identificationGuiWrappersBindingList.clear();
        identificationGuiWrappersBindingList.addAll(mainController.getPrideSpectrumAnnotator().getSpectrumAnnotatorResult().getIdentificationGuiWrappers());
    }

    public void init() {
        identificationsPanel = new IdentificationsPanel();

        //init bindings
        bindingGroup = new BindingGroup();
        identificationGuiWrappersBindingList = ObservableCollections.observableList(mainController.getPrideSpectrumAnnotator().getSpectrumAnnotatorResult().getIdentificationGuiWrappers());

        //table binding        
        JTableBinding identificationGuiWrappersTableBinding = SwingBindings.createJTableBinding(AutoBinding.UpdateStrategy.READ, identificationGuiWrappersBindingList, identificationsPanel.getIdentificationsTable());

        //Add column bindings
        ColumnBinding columnBinding = identificationGuiWrappersTableBinding.addColumnBinding(ELProperty.create("${explanationType}"));
        columnBinding.setColumnName("Pipeline explanation");
        columnBinding.setEditable(Boolean.FALSE);
        columnBinding.setColumnClass(IdentificationGuiWrapper.ExplanationType.class);
        columnBinding.setRenderer(new ExplanationCellRenderer());

        columnBinding = identificationGuiWrappersTableBinding.addColumnBinding(ELProperty.create("${identification.peptide.sequence}"));
        columnBinding.setColumnName("Peptide");
        columnBinding.setEditable(Boolean.FALSE);
        columnBinding.setColumnClass(AminoAcidSequence.class);

        columnBinding = identificationGuiWrappersTableBinding.addColumnBinding(ELProperty.create("${identification.peptide.charge}"));
        columnBinding.setColumnName("Charge");
        columnBinding.setEditable(Boolean.FALSE);
        columnBinding.setColumnClass(Integer.class);

        columnBinding = identificationGuiWrappersTableBinding.addColumnBinding(ELProperty.create("${identification.peptide}"));
        columnBinding.setColumnName("Delta m/z");
        columnBinding.setEditable(Boolean.FALSE);
        columnBinding.setColumnClass(Peptide.class);
        columnBinding.setRenderer(new DeltaMzTableCellRenderer());

        columnBinding = identificationGuiWrappersTableBinding.addColumnBinding(ELProperty.create("${identification.peptide.mzRatio}"));
        columnBinding.setColumnName("Precursor m/z");
        columnBinding.setEditable(Boolean.FALSE);
        columnBinding.setColumnClass(Double.class);        

        columnBinding = identificationGuiWrappersTableBinding.addColumnBinding(ELProperty.create("${identification.spectrumId}"));
        columnBinding.setColumnName("Pride spectrum ID");
        columnBinding.setEditable(Boolean.FALSE);
        columnBinding.setColumnClass(String.class);

        columnBinding = identificationGuiWrappersTableBinding.addColumnBinding(ELProperty.create("${identification.peptide}"));
        columnBinding.setColumnName("Modifications");
        columnBinding.setEditable(Boolean.FALSE);
        columnBinding.setColumnClass(Peptide.class);
        columnBinding.setRenderer(new ModificationsCellRenderer());

        bindingGroup.addBinding(identificationGuiWrappersTableBinding);
        bindingGroup.bind();

        //add listeners
        identificationsPanel.getIdentificationsTable().getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent lse) {
                if (!lse.getValueIsAdjusting()) {
                    if (identificationsPanel.getIdentificationsTable().getSelectedRow() != -1) {
                        IdentificationGuiWrapper identificationGuiWrapper = identificationGuiWrappersBindingList.get(identificationsPanel.getIdentificationsTable().getSelectedRow());
                        
                        System.out.println("spectrum ref: " + identificationGuiWrapper.getIdentification().getSpectrumRef() + ", spectrum id: " + identificationGuiWrapper.getIdentification().getSpectrumId());

                        SpectrumPanel spectrumPanel = spectrumPanelService.getSpectrumPanel(identificationGuiWrapper.getIdentification());

                        //remove spectrum panel if already present
                        if (identificationsPanel.getIdentificationDetailPanel().getComponentCount() != 0) {
                            identificationsPanel.getIdentificationDetailPanel().remove(0);
                        }

                        //add the spectrum panel to the identifications detail panel
                        GridBagConstraints gridBagConstraints = new GridBagConstraints();
                        gridBagConstraints.fill = GridBagConstraints.BOTH;
                        gridBagConstraints.weightx = 1.0;
                        gridBagConstraints.weighty = 1.0;

                        identificationsPanel.getIdentificationDetailPanel().add(spectrumPanel, gridBagConstraints);
                        identificationsPanel.getIdentificationDetailPanel().validate();
                        identificationsPanel.getIdentificationDetailPanel().repaint();
                    }
                }
            }
        });
    }

    private class DeltaMzTableCellRenderer extends DefaultTableCellRenderer {

        @Override
        protected void setValue(Object value) {
            Peptide peptide = (Peptide) value;
            double deltaMz = 0.0;
            try {
                BigDecimal bigDecimal = new BigDecimal(Double.toString(peptide.calculateMassDelta() / peptide.getCharge()));
                bigDecimal = bigDecimal.setScale(NUMBER_OF_DECIMALS, BigDecimal.ROUND_HALF_UP);
                deltaMz = bigDecimal.doubleValue();
            } catch (AASequenceMassUnknownException ex) {
                LOGGER.error(ex.getMessage(), ex);
            }
            super.setValue(deltaMz);
        }
    }        

    private class ModificationsCellRenderer extends DefaultTableCellRenderer {

        @Override
        protected void setValue(Object value) {
            Peptide peptide = (Peptide) value;
            String modificationsInfoString = "0";
            if (peptide instanceof ModifiedPeptide) {
                List<String> modifications = new ArrayList<String>();

                ModifiedPeptide modifiedPeptide = (ModifiedPeptide) peptide;
                if (modifiedPeptide.getNTermMod() != null) {
                    modifications.add(modifiedPeptide.getNTermMod().getName());
                }
                if (modifiedPeptide.getNTModifications() != null) {
                    for (int i = 0; i < modifiedPeptide.getNTModifications().length; i++) {
                        ModificationFacade modificationFacade = modifiedPeptide.getNTModifications()[i];
                        if (modificationFacade != null) {
                            modifications.add(modificationFacade.getName());
                        }
                    }
                }
                if (modifiedPeptide.getCTermMod() != null) {
                    modifications.add(modifiedPeptide.getCTermMod().getName());
                }

                Joiner joiner = Joiner.on(", ");
                modificationsInfoString = modifications.size() + "(" + joiner.join(modifications) + ")";
            }

            super.setValue(modificationsInfoString);
        }
    }

    private class ExplanationCellRenderer extends DefaultTableCellRenderer {

        @Override
        protected void setValue(Object value) {
            IdentificationGuiWrapper.ExplanationType explanationType = (IdentificationGuiWrapper.ExplanationType) value;

            if (explanationType.equals(IdentificationGuiWrapper.ExplanationType.UNMODIFIED)) {
                setBackground(Color.GREEN);
            } else if (explanationType.equals(IdentificationGuiWrapper.ExplanationType.MODIFIED)) {
                setBackground(Color.ORANGE);
            } else {
                setBackground(Color.RED);
            }

            super.setValue(explanationType.name());
        }
    }
}
