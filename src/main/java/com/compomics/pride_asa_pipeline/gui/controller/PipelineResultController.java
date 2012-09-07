/*
 *

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
import com.compomics.pride_asa_pipeline.model.FragmentIonAnnotation;
import com.compomics.pride_asa_pipeline.model.Identification;
import com.compomics.pride_asa_pipeline.model.Modification;
import com.compomics.pride_asa_pipeline.model.ModificationFacade;
import com.compomics.pride_asa_pipeline.model.ModifiedPeptide;
import com.compomics.pride_asa_pipeline.model.Peptide;
import com.compomics.pride_asa_pipeline.model.SpectrumAnnotatorResult;
import com.compomics.pride_asa_pipeline.model.comparator.IdentificationSequenceComparator;
import com.compomics.pride_asa_pipeline.service.ModificationService;
import com.compomics.pride_asa_pipeline.service.SpectrumPanelService;
import com.compomics.pride_asa_pipeline.util.GuiUtils;
import com.compomics.pride_asa_pipeline.util.MathUtils;
import com.compomics.util.gui.spectrum.SpectrumPanel;
import com.google.common.base.Joiner;
import com.google.common.primitives.Doubles;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.geom.RectangularShape;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.apache.log4j.Logger;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.statistics.HistogramType;
import org.jfree.ui.RectangleEdge;

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
    private static final int NUMBER_OF_PRECURSOR_MASS_DELTA_BINS = 100;
    private static final int NUMBER_OF_ION_FRAGMENT_MASS_DELTA_BINS = 50;
    private static final int NUMBER_OF_ION_COVERAGE_BINS = 100;
    private static final int NUMBER_OF_SCORE_BINS = 100;
    private static final Color[] PIE_COLORS = new Color[]{Color.GREEN, Color.ORANGE, Color.RED};
    //model
    private SpectrumAnnotatorResult spectrumAnnotatorResult;
    private EventList<Identification> identificationsEventList;
    private SortedList<Identification> sortedIdentificationsList;
    //views
    private IdentificationsPanel identificationsPanel;
    private SummaryPanel summaryPanel;
    private ChartPanel precursorMassDeltasChartPanel;
    private ChartPanel fragmentIonMassDeltasChartPanel;
    private ChartPanel ionCoverageChartPanel;
    private ChartPanel scoresChartPanel;
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
        precursorMassDeltasChartPanel.setChart(null);
        fragmentIonMassDeltasChartPanel.setChart(null);
        ionCoverageChartPanel.setChart(null);
        scoresChartPanel.setChart(null);
        identificationsChartPanel.setChart(null);
        modificationsChartPanel.setChart(null);
    }

    private void initIdentificationsPanel() {
        identificationsPanel = new IdentificationsPanel();

        identificationsEventList = new BasicEventList<Identification>();
        sortedIdentificationsList = new SortedList<Identification>(identificationsEventList, new IdentificationSequenceComparator());
        identificationsPanel.getIdentificationsTable().setModel(new EventTableModel(sortedIdentificationsList, new IdentificationsTableFormat()));
        identificationsPanel.getIdentificationsTable().setSelectionModel(new EventSelectionModel(sortedIdentificationsList));

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
        double[] precursorMassDeltaValues = new double[spectrumAnnotatorResult.getNumberOfIdentifications()];
        List<Double> fragmentMassDeltaValues = new ArrayList<Double>();
        double[] bIonCoverageValues = new double[spectrumAnnotatorResult.getNumberOfIdentifications()];
        double[] yIonCoverageValues = new double[spectrumAnnotatorResult.getNumberOfIdentifications()];
        double[] scoresValues = new double[spectrumAnnotatorResult.getNumberOfIdentifications()];

        //iterate over identifications
        for (int i = 0; i < spectrumAnnotatorResult.getNumberOfIdentifications(); i++) {
            if (spectrumAnnotatorResult.getIdentifications().get(i).getAnnotationData() != null) {
                try {
                    precursorMassDeltaValues[i] = spectrumAnnotatorResult.getIdentifications().get(i).getPeptide().calculateMassDelta();
                } catch (AASequenceMassUnknownException ex) {
                    LOGGER.error(ex.getMessage(), ex);
                }
                fragmentMassDeltaValues.addAll(calculateFragmentIonMassDeltas(spectrumAnnotatorResult.getIdentifications().get(i)));
                bIonCoverageValues[i] = calculateIonCoverages(spectrumAnnotatorResult.getIdentifications().get(i)).get(FragmentIonAnnotation.IonType.B_ION);
                yIonCoverageValues[i] = calculateIonCoverages(spectrumAnnotatorResult.getIdentifications().get(i)).get(FragmentIonAnnotation.IonType.Y_ION);
                scoresValues[i] = spectrumAnnotatorResult.getIdentifications().get(i).getAnnotationData().getIdentificationScore().getAverageFragmentIonScore();
            }
        }

        //create new precursor mass delta dataset
        HistogramDataset precMassDeltasDataset = new HistogramDataset();
        precMassDeltasDataset.setType(HistogramType.FREQUENCY);

        //sort array in order to find min and max values
        Arrays.sort(precursorMassDeltaValues);
        precMassDeltasDataset.addSeries("precursorMassDeltaSeries", precursorMassDeltaValues, NUMBER_OF_PRECURSOR_MASS_DELTA_BINS, precursorMassDeltaValues[0] - 0.5, precursorMassDeltaValues[precursorMassDeltaValues.length - 1] + 0.5);
        JFreeChart precursorMassDeltasChart = ChartFactory.createHistogram(
                "Precursor mass delta", "mass delta (d.)", "frequency", precMassDeltasDataset,
                PlotOrientation.VERTICAL, Boolean.FALSE, Boolean.TRUE, Boolean.FALSE);
        precursorMassDeltasChart.getPlot().setBackgroundPaint(Color.WHITE);
        GuiUtils.setShadowVisible(precursorMassDeltasChart, Boolean.FALSE);

        //create new fragment ion mass delta dataset
        HistogramDataset fragMassDeltasDataset = new HistogramDataset();
        fragMassDeltasDataset.setType(HistogramType.FREQUENCY);

        //sort array in order to find min and max values
        Collections.sort(fragmentMassDeltaValues);
        fragMassDeltasDataset.addSeries("precursorMassDeltaSeries", Doubles.toArray(fragmentMassDeltaValues), NUMBER_OF_ION_FRAGMENT_MASS_DELTA_BINS, fragmentMassDeltaValues.get(0) - 0.5, fragmentMassDeltaValues.get(fragmentMassDeltaValues.size() - 1) + 0.5);
        JFreeChart fragMassDeltasDeltasChart = ChartFactory.createHistogram(
                "Fragment ion mass delta", "mass delta (d.)", "frequency", fragMassDeltasDataset,
                PlotOrientation.VERTICAL, Boolean.FALSE, Boolean.TRUE, Boolean.FALSE);
        fragMassDeltasDeltasChart.getPlot().setBackgroundPaint(Color.WHITE);
        GuiUtils.setShadowVisible(fragMassDeltasDeltasChart, Boolean.FALSE);

        //create new identification scores dataset
        HistogramDataset ionCoverageDataset = new HistogramDataset();
        ionCoverageDataset.setType(HistogramType.FREQUENCY);

        ionCoverageDataset.addSeries("y ions", yIonCoverageValues, NUMBER_OF_ION_COVERAGE_BINS, 0.0, 100.0);
        ionCoverageDataset.addSeries("b ions", bIonCoverageValues, NUMBER_OF_ION_COVERAGE_BINS, 0.0, 100.0);
        JFreeChart ionCoverageChart = ChartFactory.createHistogram(
                "B/Y ion coverage", "coverage (%)", "frequency", ionCoverageDataset,
                PlotOrientation.VERTICAL, Boolean.TRUE, Boolean.TRUE, Boolean.FALSE);
        ionCoverageChart.getPlot().setBackgroundPaint(Color.WHITE);
        GuiUtils.setShadowVisible(ionCoverageChart, Boolean.FALSE);

        //create new identification scores dataset
        HistogramDataset scoresDataset = new HistogramDataset();
        scoresDataset.setType(HistogramType.FREQUENCY);

        scoresDataset.addSeries("scoreSeries", scoresValues, NUMBER_OF_SCORE_BINS, 0.0, 1.0);
        JFreeChart scoresChart = ChartFactory.createHistogram(
                "Fragment ion score", "score", "frequency", scoresDataset,
                PlotOrientation.VERTICAL, Boolean.FALSE, Boolean.TRUE, Boolean.FALSE);
        scoresChart.getPlot().setBackgroundPaint(Color.WHITE);
        GuiUtils.setShadowVisible(scoresChart, Boolean.FALSE);

        //create new identificiations data set
        DefaultPieDataset identificationsDataset = new DefaultPieDataset();
        identificationsDataset.setValue(UNMODIFIED_LABEL + "(" + spectrumAnnotatorResult.getUnmodifiedPrecursors().size() + ", " + (MathUtils.roundDouble((double) spectrumAnnotatorResult.getUnmodifiedPrecursors().size() / spectrumAnnotatorResult.getNumberOfIdentifications(), 2)) + "%)", spectrumAnnotatorResult.getUnmodifiedPrecursors().size());
        identificationsDataset.setValue(MODIFIED_LABEL + "(" + spectrumAnnotatorResult.getModifiedPrecursors().size() + ", " + (MathUtils.roundDouble((double) spectrumAnnotatorResult.getModifiedPrecursors().size() / spectrumAnnotatorResult.getNumberOfIdentifications(), 2)) + "%)", spectrumAnnotatorResult.getModifiedPrecursors().size());
        identificationsDataset.setValue(UNEXPLAINED_LABEL + "(" + spectrumAnnotatorResult.getUnexplainedIdentifications().size() + ", " + (MathUtils.roundDouble((double) spectrumAnnotatorResult.getUnexplainedIdentifications().size() / spectrumAnnotatorResult.getNumberOfIdentifications(), 2)) + "%)", spectrumAnnotatorResult.getUnexplainedIdentifications().size());

        JFreeChart identificationsChart = ChartFactory.createPieChart(
                "Identifications(" + spectrumAnnotatorResult.getNumberOfIdentifications() + ")", // chart title
                identificationsDataset, // data
                Boolean.TRUE, // include legend
                Boolean.FALSE,
                Boolean.FALSE);

        PiePlot plot = (PiePlot) identificationsChart.getPlot();
        plot.setLabelFont(new Font("SansSerif", Font.PLAIN, 12));
        plot.setLabelGenerator(null);
        plot.setNoDataMessage("No data available");
        plot.setCircular(Boolean.TRUE);
        plot.setLabelGap(0.02);
        plot.setBackgroundPaint(Color.WHITE);
        //change pie colors
        for (int i = 0; i < identificationsDataset.getItemCount(); i++) {
            Comparable key = identificationsDataset.getKey(i);
            plot.setSectionPaint(key, PIE_COLORS[i]);
        }

        //create new modifications data set
        DefaultCategoryDataset modificationsDataset = new DefaultCategoryDataset();
        Map<Modification, Integer> modifications = modificationService.getUsedModifications(spectrumAnnotatorResult);
        for (Modification modification : modifications.keySet()) {
            double relativeCount = (double) (modifications.get(modification)) / (double) (spectrumAnnotatorResult.getNumberOfIdentifications());
            modificationsDataset.addValue(relativeCount, "relative occurance", modification.getName());
        }

        JFreeChart modificationsChart = ChartFactory.createBarChart(
                "Modifications",
                "modification",
                "relative occurance",
                modificationsDataset,
                PlotOrientation.VERTICAL, Boolean.FALSE, Boolean.TRUE, Boolean.FALSE);
        modificationsChart.getPlot().setBackgroundPaint(Color.WHITE);
        GuiUtils.setShadowVisible(modificationsChart, Boolean.FALSE);

        //add charts to panels
        precursorMassDeltasChartPanel.setChart(precursorMassDeltasChart);
        fragmentIonMassDeltasChartPanel.setChart(fragMassDeltasDeltasChart);
        ionCoverageChartPanel.setChart(ionCoverageChart);
        scoresChartPanel.setChart(scoresChart);
        identificationsChartPanel.setChart(identificationsChart);
        modificationsChartPanel.setChart(modificationsChart);
    }

    private void initSummaryPanel() {
        summaryPanel = new SummaryPanel();
        precursorMassDeltasChartPanel = new ChartPanel(null);
        precursorMassDeltasChartPanel.setOpaque(Boolean.FALSE);
        fragmentIonMassDeltasChartPanel = new ChartPanel(null);
        fragmentIonMassDeltasChartPanel.setOpaque(Boolean.FALSE);
        ionCoverageChartPanel = new ChartPanel(null);
        ionCoverageChartPanel.setOpaque(Boolean.FALSE);
        scoresChartPanel = new ChartPanel(null);
        scoresChartPanel.setOpaque(Boolean.FALSE);
        identificationsChartPanel = new ChartPanel(null);
        identificationsChartPanel.setOpaque(Boolean.FALSE);
        modificationsChartPanel = new ChartPanel(null);
        modificationsChartPanel.setOpaque(Boolean.FALSE);

        //add chartPanel                  
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;

        summaryPanel.getPrecursorMassDeltaChartParentPanel().add(precursorMassDeltasChartPanel, gridBagConstraints);
        summaryPanel.getFragmentIonMassDeltaChartParentPanel().add(fragmentIonMassDeltasChartPanel, gridBagConstraints);
        summaryPanel.getIonCoverageChartParentPanel().add(ionCoverageChartPanel, gridBagConstraints);
        summaryPanel.getScoresChartParentPanel().add(scoresChartPanel, gridBagConstraints);
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

    private Map<FragmentIonAnnotation.IonType, Double> calculateIonCoverages(Identification identification) {
        Map<FragmentIonAnnotation.IonType, Double> ionCoverages = new EnumMap<FragmentIonAnnotation.IonType, Double>(FragmentIonAnnotation.IonType.class);
        if (identification.getAnnotationData() != null && identification.getAnnotationData().getFragmentIonAnnotations() != null) {
            int numberOfBIons = 0;
            int numberOfYIons = 0;
            for (FragmentIonAnnotation fragmentIonAnnotation : identification.getAnnotationData().getFragmentIonAnnotations()) {
                if (fragmentIonAnnotation.isBIon()) {
                    numberOfBIons++;
                } else if (fragmentIonAnnotation.isYIon()) {
                    numberOfYIons++;
                }
            }
            ionCoverages.put(FragmentIonAnnotation.IonType.B_ION, ((double) numberOfBIons) / identification.getPeptide().length() * 100);
            ionCoverages.put(FragmentIonAnnotation.IonType.Y_ION, ((double) numberOfYIons) / identification.getPeptide().length() * 100);
        } else {
            ionCoverages.put(FragmentIonAnnotation.IonType.B_ION, 0.0);
            ionCoverages.put(FragmentIonAnnotation.IonType.Y_ION, 0.0);
        }
        return ionCoverages;
    }

    private List<Double> calculateFragmentIonMassDeltas(Identification identification) {
        List<Double> fragmentIonMassDeltas = new ArrayList<Double>();
        if (identification.getAnnotationData() != null && identification.getAnnotationData().getFragmentIonAnnotations() != null) {
            for (FragmentIonAnnotation fragmentIonAnnotation : identification.getAnnotationData().getFragmentIonAnnotations()) {
                if (fragmentIonAnnotation.isBIon()) {
                    fragmentIonMassDeltas.add(fragmentIonAnnotation.getMz() - identification.getPeptide().getBIonLadderMasses(fragmentIonAnnotation.getIon_charge())[fragmentIonAnnotation.getFragment_ion_number() - 1]);
                } else if (fragmentIonAnnotation.isYIon()) {
                    fragmentIonMassDeltas.add(fragmentIonAnnotation.getMz() - identification.getPeptide().getYIonLadderMasses(fragmentIonAnnotation.getIon_charge())[fragmentIonAnnotation.getFragment_ion_number() - 1]);
                }
            }
        }
        return fragmentIonMassDeltas;
    }

    //private classes    
    private class IdentificationsTableFormat implements TableFormat {

        String[] columnNames = {"Peptide", "Charge", "Mass delta", "M/Z delta", "Precursor m/z", "Noise threshold", "Score", "Modifications"};
        private static final int PEPTIDE = 0;
        private static final int CHARGE = 1;
        private static final int MASS_DELTA = 2;
        private static final int MZ_DELTA = 3;
        private static final int PRECURSOR_MZ = 4;
        private static final int NOISE_THRESHOLD = 5;
        private static final int SCORE = 6;
        private static final int MODIFICATIONS = 7;

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
                case NOISE_THRESHOLD:
                    return MathUtils.roundDouble(identification.getAnnotationData().getNoiseThreshold());
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
