/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pride_asa_pipeline.gui.controller;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.swing.EventSelectionModel;
import ca.odell.glazedlists.swing.EventTableModel;
import ca.odell.glazedlists.swing.TableComparatorChooser;
import com.compomics.pride_asa_pipeline.gui.view.IdentificationsPanel;
import com.compomics.pride_asa_pipeline.gui.view.SummaryPanel;
import com.compomics.pride_asa_pipeline.model.AASequenceMassUnknownException;
import com.compomics.pride_asa_pipeline.model.Identification;
import com.compomics.pride_asa_pipeline.model.Modification;
import com.compomics.pride_asa_pipeline.model.ModificationFacade;
import com.compomics.pride_asa_pipeline.model.ModifiedPeptide;
import com.compomics.pride_asa_pipeline.model.Peptide;
import com.compomics.pride_asa_pipeline.model.SpectrumAnnotatorResult;
import com.compomics.pride_asa_pipeline.model.comparator.IdentificationSequenceComparator;
import com.compomics.pride_asa_pipeline.service.ModificationService;
import com.compomics.pride_asa_pipeline.service.SpectrumPanelService;
import com.compomics.pride_asa_pipeline.util.MathUtils;
import com.compomics.util.gui.spectrum.SpectrumPanel;
import com.google.common.base.Joiner;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.apache.log4j.Logger;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

/**
 *
 * @author Niels Hulstaert
 */
public class PipelineResultController {

    private static final Logger LOGGER = Logger.getLogger(PipelineResultController.class);
    private static final String UNMOD_MASS_DELTA_OPEN = "[";
    private static final String UNMOD_MASS_DELTA_CLOSE = "]";
    private static final String MODIFIED_LABEL = "modified";
    private static final String UNMODIFIED_LABEL = "unmodified";
    private static final String UNEXPLAINED_LABEL = "unexplained";
    //model
    private SpectrumAnnotatorResult spectrumAnnotatorResult;
    private EventList<Identification> identificationsEventList;
    private SortedList<Identification> sortedIdentificationsList;
    private EventSelectionModel identificationsSelectionModel;
    private EventTableModel identificationsTableModel;
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

    public void update(SpectrumAnnotatorResult spectrumAnnotatorResult) {
        this.spectrumAnnotatorResult = spectrumAnnotatorResult;
        updateIdentifications();
        updateSummary();
    }

    /**
     * Clear the pipeline result section
     */
    public void clear() {
        //clear identifications panel
        identificationsEventList.clear();
        addSpectrumPanel(null);

        //clear summary panel
        identificationsChartPanel.setChart(null);
        identificationsChartPanel.setOpaque(Boolean.FALSE);
        modificationsChartPanel.setChart(null);
        modificationsChartPanel.setOpaque(Boolean.FALSE);
    }

    private void initIdentificationsPanel() {
        identificationsPanel = new IdentificationsPanel();

        identificationsEventList = new BasicEventList<Identification>();
        sortedIdentificationsList = new SortedList<Identification>(identificationsEventList, new IdentificationSequenceComparator());
        identificationsSelectionModel = new EventSelectionModel(sortedIdentificationsList);
        identificationsTableModel = new EventTableModel(sortedIdentificationsList, new IdentificationsTableFormat());
        identificationsPanel.getIdentificationsTable().setModel(identificationsTableModel);
        identificationsPanel.getIdentificationsTable().setSelectionModel(identificationsSelectionModel);

        //use MULTIPLE_COLUMN_MOUSE to allow sorting by multiple columns
        TableComparatorChooser tableSorter = TableComparatorChooser.install(
                identificationsPanel.getIdentificationsTable(), sortedIdentificationsList, TableComparatorChooser.MULTIPLE_COLUMN_MOUSE);

        //add listeners
        identificationsPanel.getIdentificationsTable().getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent lse) {
                if (!lse.getValueIsAdjusting()) {
                    if (identificationsPanel.getIdentificationsTable().getSelectedRow() != -1) {
                        Identification identification = sortedIdentificationsList.get(identificationsPanel.getIdentificationsTable().getSelectedRow());

                        SpectrumPanel spectrumPanel = spectrumPanelService.getSpectrumPanel(identification);

                        addSpectrumPanel(spectrumPanel);
                    }
                }
            }
        });
    }

    private void updateIdentifications() {
        identificationsEventList.clear();
        identificationsEventList.addAll(spectrumAnnotatorResult.getIdentifications());
        addSpectrumPanel(null);
    }

    private void updateSummary() {
        //create new identificiations data set
        identificationsDataset = new DefaultPieDataset();
        identificationsDataset.setValue(UNMODIFIED_LABEL, spectrumAnnotatorResult.getUnmodifiedPrecursors().size());
        identificationsDataset.setValue(MODIFIED_LABEL, spectrumAnnotatorResult.getModifiedPrecursors().size());
        identificationsDataset.setValue(UNEXPLAINED_LABEL, spectrumAnnotatorResult.getUnexplainedIdentifications().size());

        JFreeChart identificationsChart = ChartFactory.createPieChart(
                "Identifications(" + spectrumAnnotatorResult.getNumberOfIdentifications() + ")", // chart title
                identificationsDataset, // data
                Boolean.TRUE, // include legend
                Boolean.TRUE,
                Boolean.FALSE);

        PiePlot plot = (PiePlot) identificationsChart.getPlot();
        plot.setLabelFont(new Font("SansSerif", Font.PLAIN, 12));
        plot.setNoDataMessage("No data available");
        plot.setCircular(Boolean.TRUE);
        plot.setLabelGap(0.02);

        //create new modifications data set
        modificationsDataset = new DefaultCategoryDataset();
        Map<Modification, Integer> modifications = modificationService.getUsedModifications(spectrumAnnotatorResult);
        for (Modification modification : modifications.keySet()) {
            double relativeCount = (double) (modifications.get(modification)) / (double) (spectrumAnnotatorResult.getNumberOfIdentifications());
            modificationsDataset.addValue(relativeCount, "relative count", modification.getName());
        }

        JFreeChart modificationsChart = ChartFactory.createBarChart(
                "Modifications",
                "modification",
                "relative count",
                modificationsDataset,
                PlotOrientation.VERTICAL, true, true, false);

        //add chart to panels
        identificationsChartPanel.setChart(identificationsChart);
        modificationsChartPanel.setChart(modificationsChart);
    }

    private void initSummaryPanel() {
        summaryPanel = new SummaryPanel();
        identificationsChartPanel = new ChartPanel(null);
        identificationsChartPanel.setOpaque(Boolean.FALSE);
        modificationsChartPanel = new ChartPanel(null);
        modificationsChartPanel.setOpaque(Boolean.FALSE);

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
    private class IdentificationsTableFormat implements TableFormat {

        String[] columnNames = {"Peptide", "Charge", "Mass delta", "M/Z delta", "Precursor m/z", "Score", "Modifications"};
        private static final int PEPTIDE = 0;
        private static final int CHARGE = 1;
        private static final int MASS_DELTA = 2;
        private static final int MZ_DELTA = 3;
        private static final int PRECURSOR_MZ = 4;
        private static final int SCORE = 5;
        private static final int MODIFICATIONS = 6;

        @Override
        public int getColumnCount() {
            return columnNames.length;
        }

        @Override
        public String getColumnName(int column) {
            return columnNames[column];
        }

        @Override
        public Object getColumnValue(Object object, int column) {
            Identification identification = (Identification) object;
            switch (column) {
                case PEPTIDE:
                    return identification.getPeptide().getSequenceString();
                case CHARGE:
                    return identification.getPeptide().getCharge();
                case MASS_DELTA:
                    return constructMassDeltaString(identification.getPeptide(), Boolean.FALSE);
                case MZ_DELTA:
                    return constructMassDeltaString(identification.getPeptide(), Boolean.TRUE);
                case PRECURSOR_MZ:
                    return MathUtils.roundDouble(identification.getPeptide().getMzRatio());
                case SCORE:
                    return MathUtils.roundDouble(identification.getAnnotationData().getIdentificationScore().getAverageFragmentIonScore());
                case MODIFICATIONS:
                    return constructModificationsString(identification.getPeptide());
                default:
                    throw new IllegalArgumentException("Unexpected column number " + column);
            }
        }

        private String constructModificationsString(Peptide peptide) {
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

            return modificationsInfoString;
        }

        private String constructMassDeltaString(Peptide peptide, boolean doChargeAdjustment) {
            String massDelta = "N/A";
            try {
                double massDeltaValue = peptide.calculateMassDelta();
                if (doChargeAdjustment) {
                    massDeltaValue = massDeltaValue / peptide.getCharge();
                }
                massDelta = Double.toString(MathUtils.roundDouble(massDeltaValue));
                //check if the peptide is a modified peptide,
                //if so, show the corrected mass delta as well.
                if (peptide instanceof ModifiedPeptide) {
                    double massDeltaValueWithMods = peptide.calculateMassDelta() - ((ModifiedPeptide) peptide).calculateModificationsMass();
                    if (doChargeAdjustment) {
                        massDeltaValueWithMods = massDeltaValueWithMods / peptide.getCharge();
                    }
                    massDelta = Double.toString(MathUtils.roundDouble(massDeltaValueWithMods)) + " " + UNMOD_MASS_DELTA_OPEN + massDelta + UNMOD_MASS_DELTA_CLOSE;
                }
            } catch (AASequenceMassUnknownException ex) {
                LOGGER.error(ex.getMessage(), ex);
            }

            return massDelta;
        }
    }
}
