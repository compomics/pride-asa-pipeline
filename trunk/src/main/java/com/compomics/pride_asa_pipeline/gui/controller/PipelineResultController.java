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
import com.compomics.pride_asa_pipeline.gui.ChartFactory;
import com.compomics.pride_asa_pipeline.gui.view.IdentificationsPanel;
import com.compomics.pride_asa_pipeline.gui.view.SummaryPanel;
import com.compomics.pride_asa_pipeline.model.AASequenceMassUnknownException;
import com.compomics.pride_asa_pipeline.model.FragmentIonAnnotation;
import com.compomics.pride_asa_pipeline.model.Identification;
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
import java.awt.GridBagConstraints;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.apache.log4j.Logger;
import org.jfree.chart.ChartPanel;

/**
 *
 * @author Niels Hulstaert
 */
public class PipelineResultController {

    private static final Logger LOGGER = Logger.getLogger(PipelineResultController.class);
    private static final String UNMOD_MASS_DELTA_OPEN = "[";
    private static final String UNMOD_MASS_DELTA_CLOSE = "]";
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

        //get charts from factory and add them to the right panels
        precursorMassDeltasChartPanel.setChart(ChartFactory.getPrecursorMassDeltasChart(precursorMassDeltaValues));
        fragmentIonMassDeltasChartPanel.setChart(ChartFactory.getFragmentMassDeltasChart(fragmentMassDeltaValues));
        ionCoverageChartPanel.setChart(ChartFactory.getIonCoverageChart(bIonCoverageValues, yIonCoverageValues));
        scoresChartPanel.setChart(ChartFactory.getScoresChart(scoresValues));
        identificationsChartPanel.setChart(ChartFactory.getIdentificationsChart(spectrumAnnotatorResult));
        modificationsChartPanel.setChart(ChartFactory.getModificationsChart(modificationService.getUsedModifications(spectrumAnnotatorResult), spectrumAnnotatorResult.getNumberOfIdentifications()));
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
                if (fragmentIonAnnotation.isBIon() && fragmentIonAnnotation.getIon_charge() == 1) {
                    numberOfBIons++;
                } else if (fragmentIonAnnotation.isYIon() && fragmentIonAnnotation.getIon_charge() == 1) {
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
