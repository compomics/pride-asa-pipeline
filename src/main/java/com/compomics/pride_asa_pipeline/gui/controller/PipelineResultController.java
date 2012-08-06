/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pride_asa_pipeline.gui.controller;

import com.compomics.pride_asa_pipeline.gui.view.PipelineResultPanel;
import com.compomics.pride_asa_pipeline.model.AASequenceMassUnknownException;
import com.compomics.pride_asa_pipeline.model.AminoAcidSequence;
import com.compomics.pride_asa_pipeline.model.Identification;
import com.compomics.pride_asa_pipeline.model.ModificationFacade;
import com.compomics.pride_asa_pipeline.model.ModifiedPeptide;
import com.compomics.pride_asa_pipeline.model.Peptide;
import com.google.common.base.Joiner;
import java.awt.Component;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
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
    private ObservableList<Identification> identificationsBindingList;
    //view
    private PipelineResultPanel pipelineResultPanel;
    //parent controller
    private MainController mainController;

    public MainController getMainController() {
        return mainController;
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public PipelineResultPanel getPipelineResultPanel() {
        return pipelineResultPanel;
    }

    public void updateIdentifications() {
        identificationsBindingList.clear();
        identificationsBindingList.addAll(mainController.getPrideSpectrumAnnotator().getSpectrumAnnotatorResult().getAllIdentifications());
    }

    public void init() {
        pipelineResultPanel = new PipelineResultPanel();

        //init bindings
        bindingGroup = new BindingGroup();
        identificationsBindingList = ObservableCollections.observableList(mainController.getPrideSpectrumAnnotator().getSpectrumAnnotatorResult().getAllIdentifications());

        //table binding        
        JTableBinding identificationsTableBinding = SwingBindings.createJTableBinding(AutoBinding.UpdateStrategy.READ, identificationsBindingList, pipelineResultPanel.getIdentificationsTable());

        //Add column bindings
        ColumnBinding columnBinding = identificationsTableBinding.addColumnBinding(ELProperty.create("${peptide.sequence}"));
        columnBinding.setColumnName("Peptide");
        columnBinding.setEditable(Boolean.FALSE);
        columnBinding.setColumnClass(AminoAcidSequence.class);

        columnBinding = identificationsTableBinding.addColumnBinding(ELProperty.create("${peptide.charge}"));
        columnBinding.setColumnName("Charge");
        columnBinding.setEditable(Boolean.FALSE);
        columnBinding.setColumnClass(Integer.class);

        columnBinding = identificationsTableBinding.addColumnBinding(ELProperty.create("${peptide}"));
        columnBinding.setColumnName("Delta m/z");
        columnBinding.setEditable(Boolean.FALSE);
        columnBinding.setColumnClass(Peptide.class);
        columnBinding.setRenderer(new DeltaMzTableCellRenderer());

        columnBinding = identificationsTableBinding.addColumnBinding(ELProperty.create("${peptide.mzRatio}"));
        columnBinding.setColumnName("Precursor m/z");
        columnBinding.setEditable(Boolean.FALSE);
        columnBinding.setColumnClass(Double.class);

        columnBinding = identificationsTableBinding.addColumnBinding(ELProperty.create("${spectrumId}"));
        columnBinding.setColumnName("Pride spectrum ID");
        columnBinding.setEditable(Boolean.FALSE);
        columnBinding.setColumnClass(String.class);

        columnBinding = identificationsTableBinding.addColumnBinding(ELProperty.create("${peptide}"));
        columnBinding.setColumnName("Modifications");
        columnBinding.setEditable(Boolean.FALSE);
        columnBinding.setColumnClass(Peptide.class);
        columnBinding.setRenderer(new ModificationsTableCellRenderer());

        bindingGroup.addBinding(identificationsTableBinding);
        bindingGroup.bind();
    }

    private class DeltaMzTableCellRenderer extends DefaultTableCellRenderer {

        @Override
        protected void setValue(Object value) {
            Peptide peptide = (Peptide) value;
            double deltaMz = 0.0;
            try {
                BigDecimal bigDecimal = new BigDecimal(Double.toString(peptide.calculateMassDelta()));
                bigDecimal = bigDecimal.setScale(NUMBER_OF_DECIMALS, BigDecimal.ROUND_HALF_UP);
                deltaMz = bigDecimal.doubleValue();
            } catch (AASequenceMassUnknownException ex) {
                LOGGER.error(ex.getMessage(), ex);
            }
            super.setValue(deltaMz);
        }
    }

    private class ModificationsTableCellRenderer extends DefaultTableCellRenderer {

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
}
