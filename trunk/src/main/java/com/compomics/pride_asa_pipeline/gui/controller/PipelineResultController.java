/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pride_asa_pipeline.gui.controller;

import com.compomics.pride_asa_pipeline.gui.view.IdentificationsPanel;
import com.compomics.pride_asa_pipeline.gui.view.SummaryPanel;
import com.compomics.pride_asa_pipeline.model.AminoAcidSequence;
import com.compomics.pride_asa_pipeline.model.Identification;
import com.compomics.pride_asa_pipeline.model.IdentificationScore;
import com.compomics.pride_asa_pipeline.model.Modification;
import com.compomics.pride_asa_pipeline.model.ModificationFacade;
import com.compomics.pride_asa_pipeline.model.ModifiedPeptide;
import com.compomics.pride_asa_pipeline.model.Peptide;
import com.compomics.pride_asa_pipeline.service.ModificationService;
import com.compomics.pride_asa_pipeline.service.SpectrumPanelService;
import com.compomics.pride_asa_pipeline.util.GuiUtils;
import com.compomics.util.gui.spectrum.SpectrumPanel;
import com.google.common.base.Joiner;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

/**
 *
 * @author niels
 */
public class PipelineResultController {

    private static final Logger LOGGER = Logger.getLogger(PipelineResultController.class);
    
    private static final String MODIFIED_LABEL = "modified";
    private static final String UNMODIFIED_LABEL = "unmodified";
    private static final String UNEXPLAINED_LABEL = "unexplained";
    
    //model
    private BindingGroup bindingGroup;
    private ObservableList<Identification> identificationsBindingList;
    private DefaultPieDataset identificationsDataset;
    private DefaultCategoryDataset modificationsDataset;
    //views
    private IdentificationsPanel identificationsPanel;
    private SummaryPanel summaryPanel;
    private ChartPanel identificationsChartPanel;
    private ChartPanel modificationsChartPanel;
    //parent controller
    private MainController mainController;
    //services
    private SpectrumPanelService spectrumPanelService;
    private ModificationService modificationService;

    public MainController getMainController() {
        return mainController;
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public IdentificationsPanel getIdentificationsPanel() {
        return identificationsPanel;
    }

    public SummaryPanel getSummaryPanel() {
        return summaryPanel;
    }

    public SpectrumPanelService getSpectrumPanelService() {
        return spectrumPanelService;
    }

    public void setSpectrumPanelService(SpectrumPanelService spectrumPanelService) {
        this.spectrumPanelService = spectrumPanelService;
    }

    public ModificationService getModificationService() {
        return modificationService;
    }

    public void setModificationService(ModificationService modificationService) {
        this.modificationService = modificationService;
    }

    public void init() {
        initIdentificationsPanel();
        initSummaryPanel();
    }

    public void updateIdentifications() {
        identificationsBindingList.clear();
        identificationsBindingList.addAll(mainController.getPrideSpectrumAnnotator().getSpectrumAnnotatorResult().getIdentifications());
    }

    public void updateSummary() {
        //create new identificiations data set
        identificationsDataset = new DefaultPieDataset();
        identificationsDataset.setValue(UNMODIFIED_LABEL, mainController.getPrideSpectrumAnnotator().getSpectrumAnnotatorResult().getUnmodifiedPrecursors().size());
        identificationsDataset.setValue(MODIFIED_LABEL, mainController.getPrideSpectrumAnnotator().getSpectrumAnnotatorResult().getModifiedPrecursors().size());
        identificationsDataset.setValue(UNEXPLAINED_LABEL, mainController.getPrideSpectrumAnnotator().getSpectrumAnnotatorResult().getUnexplainedIdentifications().size());

        JFreeChart identificationsChart = ChartFactory.createPieChart(
                "Identifications", // chart title
                identificationsDataset, // data
                Boolean.TRUE, // include legend
                Boolean.TRUE,
                Boolean.FALSE);

        PiePlot plot = (PiePlot) identificationsChart.getPlot();
        plot.setLabelFont(new Font("SansSerif", Font.PLAIN, 12));
        plot.setNoDataMessage("No data available");
        //plot.setCircular(false);
        plot.setLabelGap(0.02);

        //create new modifications data set
        modificationsDataset = new DefaultCategoryDataset();
        Map<Modification, Integer> modifications = modificationService.getUsedModifications(mainController.getPrideSpectrumAnnotator().getSpectrumAnnotatorResult());
        for (Modification modification : modifications.keySet()) {
            double relativeCount = (double) (modifications.get(modification)) / (double) (mainController.getPrideSpectrumAnnotator().getSpectrumAnnotatorResult().getNumberOfIdentifications());
            modificationsDataset.addValue(relativeCount, "relative count", modification.getName());
        }

        JFreeChart modificationsChart = ChartFactory.createBarChart(
                "Modifications",
                "modification",
                "relative count",
                modificationsDataset,
                PlotOrientation.VERTICAL, true, true, false);
        CategoryPlot modificationsPlot = (CategoryPlot) modificationsChart.getPlot();
        //CategoryAxis xAxis = (CategoryAxis) modificationsPlot.getDomainAxis();
        //xAxis.setCategoryLabelPositions(CategoryLabelPositions.DOWN_45);

        //add chart to panels
        identificationsChartPanel.setChart(identificationsChart);
        modificationsChartPanel.setChart(modificationsChart);
    }

    /**
     * Clear the pipeline result section
     */
    public void clear() {
        //clear identifications panel
        identificationsBindingList.clear();
        addSpectrumPanel(null);

        //clear summary panel
        identificationsChartPanel.setChart(null);
        modificationsChartPanel.setChart(null);
    }

    private void initIdentificationsPanel() {
        identificationsPanel = new IdentificationsPanel();

        //init bindings
        bindingGroup = new BindingGroup();
        identificationsBindingList = ObservableCollections.observableList(new ArrayList<Identification>());

        //table binding        
        JTableBinding identificationTableBinding = SwingBindings.createJTableBinding(AutoBinding.UpdateStrategy.READ, identificationsBindingList, identificationsPanel.getIdentificationsTable());

        //Add column bindings        
        ColumnBinding columnBinding = identificationTableBinding.addColumnBinding(ELProperty.create("${peptide.sequence}"));
        columnBinding.setColumnName("Peptide");
        columnBinding.setEditable(Boolean.FALSE);
        columnBinding.setColumnClass(AminoAcidSequence.class);

        columnBinding = identificationTableBinding.addColumnBinding(ELProperty.create("${peptide.charge}"));
        columnBinding.setColumnName("Charge");
        columnBinding.setEditable(Boolean.FALSE);
        columnBinding.setColumnClass(Integer.class);
        
        columnBinding = identificationTableBinding.addColumnBinding(ELProperty.create("${peptide}"));
        columnBinding.setColumnName("Delta m");
        columnBinding.setEditable(Boolean.FALSE);
        columnBinding.setColumnClass(Peptide.class);
        columnBinding.setRenderer(new DeltaMTableCellRenderer());
        
        columnBinding = identificationTableBinding.addColumnBinding(ELProperty.create("${peptide}"));
        columnBinding.setColumnName("Delta m/z");
        columnBinding.setEditable(Boolean.FALSE);
        columnBinding.setColumnClass(Peptide.class);
        columnBinding.setRenderer(new DeltaMTableCellRenderer());

        columnBinding = identificationTableBinding.addColumnBinding(ELProperty.create("${peptide.mzRatio}"));
        columnBinding.setColumnName("Precursor m/z");
        columnBinding.setEditable(Boolean.FALSE);
        columnBinding.setColumnClass(Double.class);
        columnBinding.setRenderer(new PrecursorMzRatioCellRenderer());
        
        columnBinding = identificationTableBinding.addColumnBinding(ELProperty.create("${annotationData.identificationScore}"));
        columnBinding.setColumnName("Average fragment ion score");
        columnBinding.setEditable(Boolean.FALSE);
        columnBinding.setColumnClass(IdentificationScore.class);
        columnBinding.setRenderer(new IdentificationScoreCellRenderer());

        columnBinding = identificationTableBinding.addColumnBinding(ELProperty.create("${peptide}"));
        columnBinding.setColumnName("Modifications");
        columnBinding.setEditable(Boolean.FALSE);
        columnBinding.setColumnClass(Peptide.class);
        columnBinding.setRenderer(new ModificationsCellRenderer());

        bindingGroup.addBinding(identificationTableBinding);
        bindingGroup.bind();

        //add listeners
        identificationsPanel.getIdentificationsTable().getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent lse) {
                if (!lse.getValueIsAdjusting()) {
                    if (identificationsPanel.getIdentificationsTable().getSelectedRow() != -1) {
                        Identification identification = identificationsBindingList.get(identificationsPanel.getIdentificationsTable().getSelectedRow());

                        SpectrumPanel spectrumPanel = spectrumPanelService.getSpectrumPanel(identification);

                        addSpectrumPanel(spectrumPanel);
                    }
                }
            }
        });
    }

    private void initSummaryPanel() {
        summaryPanel = new SummaryPanel();
        identificationsChartPanel = new ChartPanel(null);
        modificationsChartPanel = new ChartPanel(null);

        //add chartPanel                  
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;

        summaryPanel.getIdentificationsChartParentPanel().add(identificationsChartPanel, gridBagConstraints);
        summaryPanel.getModificationsChartParentPanel().add(modificationsChartPanel, gridBagConstraints);
    }    

    private void addSpectrumPanel(SpectrumPanel spectrumPanel) {
        //remove spectrum panel if already present
        if (identificationsPanel.getIdentificationDetailPanel().getComponentCount() != 0) {
            identificationsPanel.getIdentificationDetailPanel().remove(0);
        }

        if (spectrumPanel != null) {
            //add the spectrum panel to the identifications detail panel
            GridBagConstraints gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.fill = GridBagConstraints.BOTH;
            gridBagConstraints.weightx = 1.0;
            gridBagConstraints.weighty = 1.0;

            identificationsPanel.getIdentificationDetailPanel().add(spectrumPanel, gridBagConstraints);
        }

        identificationsPanel.getIdentificationDetailPanel().validate();
        identificationsPanel.getIdentificationDetailPanel().repaint();
    }

    //private classes
    private class DeltaMTableCellRenderer extends DefaultTableCellRenderer {                
        
        @Override
        protected void setValue(Object value) {
//            Peptide peptide = (Peptide) value;
//            String deltaMValue = null;
//            try {
//                double deltaM = peptide.calculateMassDelta();
//                //check if the peptide is a modified peptide,
//                //if so, show the corrected mass delta as well.
//                if(peptide instanceof ModifiedPeptide){
//                    deltaM
//                }
//                else{                    
//                }
//                deltaMz = GuiUtils.roundDouble(peptide.calculateMassDelta());                
//                    deltaMz = deltaMz / peptide.getCharge();
//                
//            } catch (AASequenceMassUnknownException ex) {
//                LOGGER.error(ex.getMessage(), ex);
//            }
//            super.setValue(deltaMz);
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
                
                setBackground(Color.ORANGE);
            }            

            super.setValue(modificationsInfoString);
        }
    }

//    private class ExplanationCellRenderer extends DefaultTableCellRenderer {
//
//        @Override
//        protected void setValue(Object value) {
//            IdentificationGuiWrapper.ExplanationType explanationType = (IdentificationGuiWrapper.ExplanationType) value;
//
//            if (explanationType.equals(IdentificationGuiWrapper.ExplanationType.UNMODIFIED)) {
//                setBackground(Color.GREEN);
//            } else if (explanationType.equals(IdentificationGuiWrapper.ExplanationType.MODIFIED)) {
//                setBackground(Color.ORANGE);
//            } else {
//                setBackground(Color.RED);
//            }
//
//            super.setValue(explanationType.name());
//        }
//    }

    private class IdentificationScoreCellRenderer extends DefaultTableCellRenderer {

        @Override
        protected void setValue(Object value) {
            IdentificationScore identificationScore = (IdentificationScore) value;

            super.setValue(GuiUtils.roundDouble(identificationScore.getAverageFragmentIonScore()));
        }
    }

    private class PrecursorMzRatioCellRenderer extends DefaultTableCellRenderer {

        @Override
        protected void setValue(Object value) {
            Double precursorMzRatio = (Double) value;

            super.setValue(GuiUtils.roundDouble(precursorMzRatio));
        }
    }
}
