package com.compomics.pride_asa_pipeline.gui.controller;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.swing.EventSelectionModel;
import ca.odell.glazedlists.swing.EventTableModel;
import ca.odell.glazedlists.swing.TableComparatorChooser;
import com.compomics.pride_asa_pipeline.gui.ChartFactory;
import com.compomics.pride_asa_pipeline.gui.IdentificationsTableFormat;
import com.compomics.pride_asa_pipeline.gui.SpectrumPanelFactory;
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
import com.compomics.util.gui.spectrum.SpectrumPanel;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.RenderingHints;
import java.awt.font.TextAttribute;
import java.text.AttributedString;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableCellRenderer;
import org.apache.log4j.Logger;
import org.jfree.chart.ChartPanel;

/**
 *
 * @author Niels Hulstaert
 * @author Harald Barsnes
 */
public class PipelineResultController {

    private static final Logger LOGGER = Logger.getLogger(PipelineResultController.class);
    private static final Color[] MODIFICATIONS_AVAILABLE_COLORS = {new Color(255, 215, 0), new Color(238, 130, 238), new Color(255, 140, 0), new Color(46, 139, 87), new Color(205, 92, 92)};
    //model
    private SpectrumAnnotatorResult spectrumAnnotatorResult;
    private EventList<Identification> identificationsEventList;
    private SortedList<Identification> sortedIdentificationsList;
    private Map<Modification, Integer> usedModifications;
    private Map<String, Color> modificationColors;
    //views
    private IdentificationsPanel identificationsPanel;
    private SummaryPanel summaryPanel;
    private ChartPanel precursorMassDeltasChartPanel;
    private ChartPanel fragmentIonMassDeltasChartPanel;
    private ChartPanel bIonCoverageChartPanel;
    private ChartPanel yIonCoverageChartPanel;
    private ChartPanel scoresChartPanel;
    private ChartPanel identificationsChartPanel;
    private ChartPanel modificationsChartPanel;
    //parent controller
    private MainController mainController;
    //services
    private SpectrumPanelFactory spectrumPanelFactory;
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

    public SpectrumPanelFactory getSpectrumPanelFactory() {
        return spectrumPanelFactory;
    }

    public void setSpectrumPanelFactory(SpectrumPanelFactory spectrumPanelFactory) {
        this.spectrumPanelFactory = spectrumPanelFactory;
    }

    public ModificationService getModificationService() {
        return modificationService;
    }

    public void setModificationService(ModificationService modificationService) {
        this.modificationService = modificationService;
    }

    public void init() {
        usedModifications = new HashMap<Modification, Integer>();
        modificationColors = new HashMap<String, Color>();
        initIdentificationsPanel();
        initSummaryPanel();
    }

    public void update(SpectrumAnnotatorResult spectrumAnnotatorResult) {
        this.spectrumAnnotatorResult = spectrumAnnotatorResult;
        //store used modifications        
        usedModifications = modificationService.getUsedModifications(spectrumAnnotatorResult);
        constructModificationColors();
        updateIdentifications();
        updateSummary();
    }

    /**
     * Clear the pipeline result section
     */
    public void clear() {
        //clear identifications panel
        identificationsEventList.clear();
        usedModifications.clear();
        addSpectrumPanel(null);

        //clear summary panel
        precursorMassDeltasChartPanel.setChart(null);
        fragmentIonMassDeltasChartPanel.setChart(null);
        bIonCoverageChartPanel.setChart(null);
        yIonCoverageChartPanel.setChart(null);
        scoresChartPanel.setChart(null);
        identificationsChartPanel.setChart(null);
        modificationsChartPanel.setChart(null);
    }

    private void updateIdentifications() {
        identificationsEventList.clear();
        identificationsEventList.addAll(spectrumAnnotatorResult.getIdentifications());
        addSpectrumPanel(null);
    }

    private void updateSummary() {
        double[] precursorMassDeltaValues = new double[spectrumAnnotatorResult.getNumberOfIdentifications()];
        List<Double> fragmentMassDeltaValues = new ArrayList<Double>();
        double[] b1IonCoverageValues = new double[spectrumAnnotatorResult.getNumberOfIdentifications()];
        double[] b2IonCoverageValues = new double[spectrumAnnotatorResult.getNumberOfIdentifications()];
        double[] y1IonCoverageValues = new double[spectrumAnnotatorResult.getNumberOfIdentifications()];
        double[] y2IonCoverageValues = new double[spectrumAnnotatorResult.getNumberOfIdentifications()];
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
                b1IonCoverageValues[i] = calculateIonCoverages(spectrumAnnotatorResult.getIdentifications().get(i)).get(FragmentIonAnnotation.IonType.B_ION)[0];
                b2IonCoverageValues[i] = calculateIonCoverages(spectrumAnnotatorResult.getIdentifications().get(i)).get(FragmentIonAnnotation.IonType.B_ION)[1];
                y1IonCoverageValues[i] = calculateIonCoverages(spectrumAnnotatorResult.getIdentifications().get(i)).get(FragmentIonAnnotation.IonType.Y_ION)[0];
                y2IonCoverageValues[i] = calculateIonCoverages(spectrumAnnotatorResult.getIdentifications().get(i)).get(FragmentIonAnnotation.IonType.Y_ION)[1];
                scoresValues[i] = spectrumAnnotatorResult.getIdentifications().get(i).getAnnotationData().getIdentificationScore().getAverageFragmentIonScore();
            }
        }

        //get charts from factory and add them to the right panels
        precursorMassDeltasChartPanel.setChart(ChartFactory.getPrecursorMassDeltasChart(precursorMassDeltaValues));
        fragmentIonMassDeltasChartPanel.setChart(ChartFactory.getFragmentMassDeltasChart(fragmentMassDeltaValues));
        bIonCoverageChartPanel.setChart(ChartFactory.getIonCoverageChart("B Fragment Ion Coverage", b1IonCoverageValues, b2IonCoverageValues));
        yIonCoverageChartPanel.setChart(ChartFactory.getIonCoverageChart("Y Fragment Ion Coverage", y1IonCoverageValues, y2IonCoverageValues));
        scoresChartPanel.setChart(ChartFactory.getScoresChart(scoresValues));
        identificationsChartPanel.setChart(ChartFactory.getIdentificationsChart(spectrumAnnotatorResult));
        modificationsChartPanel.setChart(ChartFactory.getModificationsChart(usedModifications, spectrumAnnotatorResult.getNumberOfIdentifications()));
    }

    private void initIdentificationsPanel() {
        identificationsPanel = new IdentificationsPanel();

        identificationsEventList = new BasicEventList<Identification>();
        sortedIdentificationsList = new SortedList<Identification>(identificationsEventList, new IdentificationSequenceComparator());
        identificationsPanel.getIdentificationsTable().setModel(new EventTableModel(sortedIdentificationsList, new IdentificationsTableFormat()));
        identificationsPanel.getIdentificationsTable().setSelectionModel(new EventSelectionModel(sortedIdentificationsList));
        identificationsPanel.getIdentificationsTable().getColumnModel().getColumn(IdentificationsTableFormat.PEPTIDE).setCellRenderer(new PeptideColumnRenderer());
        identificationsPanel.getIdentificationsTable().getColumnModel().getColumn(IdentificationsTableFormat.MODIFICATIONS).setCellRenderer(new ModificationColumnRenderer());

        //set column widths
        identificationsPanel.getIdentificationsTable().getColumnModel().getColumn(IdentificationsTableFormat.PEPTIDE_ID).setPreferredWidth(5);
        identificationsPanel.getIdentificationsTable().getColumnModel().getColumn(IdentificationsTableFormat.PEPTIDE).setPreferredWidth(200);
        identificationsPanel.getIdentificationsTable().getColumnModel().getColumn(IdentificationsTableFormat.MODIFICATIONS).setPreferredWidth(200);
        identificationsPanel.getIdentificationsTable().getColumnModel().getColumn(IdentificationsTableFormat.CHARGE).setPreferredWidth(10);
        identificationsPanel.getIdentificationsTable().getColumnModel().getColumn(IdentificationsTableFormat.MASS_DELTA).setPreferredWidth(100);
        identificationsPanel.getIdentificationsTable().getColumnModel().getColumn(IdentificationsTableFormat.MZ_DELTA).setPreferredWidth(100);
        identificationsPanel.getIdentificationsTable().getColumnModel().getColumn(IdentificationsTableFormat.PRECURSOR_MZ).setPreferredWidth(20);
        identificationsPanel.getIdentificationsTable().getColumnModel().getColumn(IdentificationsTableFormat.NOISE_THRESHOLD).setPreferredWidth(20);
        identificationsPanel.getIdentificationsTable().getColumnModel().getColumn(IdentificationsTableFormat.SCORE).setPreferredWidth(20);

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

                        SpectrumPanel spectrumPanel = spectrumPanelFactory.getSpectrumPanel(identification, mainController.getExperimentSelectionController().isPrideXml());

                        addSpectrumPanel(spectrumPanel);
                    }
                }
            }
        });
    }

    private void initSummaryPanel() {
        summaryPanel = new SummaryPanel();
        precursorMassDeltasChartPanel = new ChartPanel(null);
        precursorMassDeltasChartPanel.setOpaque(Boolean.FALSE);
        fragmentIonMassDeltasChartPanel = new ChartPanel(null);
        fragmentIonMassDeltasChartPanel.setOpaque(Boolean.FALSE);
        bIonCoverageChartPanel = new ChartPanel(null);
        bIonCoverageChartPanel.setOpaque(Boolean.FALSE);
        yIonCoverageChartPanel = new ChartPanel(null);
        yIonCoverageChartPanel.setOpaque(Boolean.FALSE);
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
        summaryPanel.getbIonCoverageChartParentPanel().add(bIonCoverageChartPanel, gridBagConstraints);
        summaryPanel.getyIonCoverageChartParentPanel().add(yIonCoverageChartPanel, gridBagConstraints);
        summaryPanel.getScoresChartParentPanel().add(scoresChartPanel, gridBagConstraints);
        summaryPanel.getIdentificationsChartParentPanel().add(identificationsChartPanel, gridBagConstraints);
        summaryPanel.getModificationsChartParentPanel().add(modificationsChartPanel, gridBagConstraints);
    }

    private void addSpectrumPanel(SpectrumPanel spectrumPanel) {
        //remove spectrum panel if already present
        if (identificationsPanel.getIdentificationDetailPanel().getComponentCount() != 0) {
            identificationsPanel.getIdentificationDetailPanel().remove(0);
        }

        //add the spectrum panel to the identifications detail panel
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        if (spectrumPanel != null) {
            identificationsPanel.getIdentificationDetailPanel().add(spectrumPanel, gridBagConstraints);
        } else {
            identificationsPanel.getIdentificationDetailPanel().add(new JPanel(), gridBagConstraints);
        }

        identificationsPanel.getIdentificationDetailPanel().validate();
        identificationsPanel.getIdentificationDetailPanel().repaint();
    }

    private Map<FragmentIonAnnotation.IonType, double[]> calculateIonCoverages(Identification identification) {
        Map<FragmentIonAnnotation.IonType, double[]> ionCoverages = new EnumMap<FragmentIonAnnotation.IonType, double[]>(FragmentIonAnnotation.IonType.class);
        if (identification.getAnnotationData() != null && identification.getAnnotationData().getFragmentIonAnnotations() != null) {
            int numberOfB1Ions = 0;
            int numberOfB2Ions = 0;
            int numberOfY1Ions = 0;
            int numberOfY2Ions = 0;
            for (FragmentIonAnnotation fragmentIonAnnotation : identification.getAnnotationData().getFragmentIonAnnotations()) {
                if (fragmentIonAnnotation.isBIon() && fragmentIonAnnotation.getIon_charge() == 1) {
                    numberOfB1Ions++;
                } else if (fragmentIonAnnotation.isBIon() && fragmentIonAnnotation.getIon_charge() == 2) {
                    numberOfB2Ions++;
                } else if (fragmentIonAnnotation.isYIon() && fragmentIonAnnotation.getIon_charge() == 1) {
                    numberOfY1Ions++;
                } else if (fragmentIonAnnotation.isYIon() && fragmentIonAnnotation.getIon_charge() == 2) {
                    numberOfY2Ions++;
                }
            }
            ionCoverages.put(FragmentIonAnnotation.IonType.B_ION,
                    new double[]{
                (double) numberOfB1Ions / identification.getPeptide().length() * 100,
                (double) numberOfB2Ions / identification.getPeptide().length() * 100
            });
            ionCoverages.put(FragmentIonAnnotation.IonType.Y_ION,
                    new double[]{
                (double) numberOfY1Ions / identification.getPeptide().length() * 100,
                (double) numberOfY2Ions / identification.getPeptide().length() * 100
            });
        } else {
            ionCoverages.put(FragmentIonAnnotation.IonType.B_ION, new double[]{0.0, 0.0});
            ionCoverages.put(FragmentIonAnnotation.IonType.Y_ION, new double[]{0.0, 0.0});
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

    private void constructModificationColors() {
        int counter = 0;
        for (Modification modification : usedModifications.keySet()) {
            modificationColors.put(modification.getName(), MODIFICATIONS_AVAILABLE_COLORS[counter % (MODIFICATIONS_AVAILABLE_COLORS.length)]);
            counter++;
        }
    }

    private class PeptideColumnRenderer extends JLabel implements TableCellRenderer {

        private AttributedString attributedSequence = null;

        public PeptideColumnRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Identification identification = sortedIdentificationsList.get(row);
            constructAttributedSequenceString(identification.getPeptide());

            if (isSelected) {
                this.setBackground(table.getSelectionBackground());
            } else {
                if (row % 2 == 0) {
                    setBackground(UIManager.getColor("Table.alternateRowColor"));
                } else {
                    setBackground(Color.WHITE);
                }
            }
            this.revalidate();
            this.repaint();

            return this;
        }

        private void constructAttributedSequenceString(Peptide peptide) {
            attributedSequence = new AttributedString(peptide.getSequenceString());
            if (peptide instanceof ModifiedPeptide) {
                ModifiedPeptide modifiedPeptide = (ModifiedPeptide) peptide;

                if (modifiedPeptide.getNTModifications() != null) {
                    for (int i = 0; i < peptide.getSequenceString().length(); i++) {
                        ModificationFacade modificationFacade = modifiedPeptide.getNTModifications()[i];
                        if (modificationFacade != null) {
                            attributedSequence.addAttribute(TextAttribute.FOREGROUND, modificationColors.get(modificationFacade.getName()), i, i + 1);
                        }
                    }
                }
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            int offset = 0;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.drawString(attributedSequence.getIterator(), offset, 15);
            g2.dispose();
        }
    }

    private class ModificationColumnRenderer extends JLabel implements TableCellRenderer {

        private AttributedString attributedModificationsString = null;

        public ModificationColumnRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            String modificationsString = (String) value;
            constructAttributedModificationsString(modificationsString);

            if (isSelected) {
                this.setBackground(table.getSelectionBackground());
            } else {
                if (row % 2 == 0) {
                    setBackground(UIManager.getColor("Table.alternateRowColor"));
                } else {
                    setBackground(Color.WHITE);
                }
            }
            this.revalidate();
            this.repaint();

            return this;
        }

        private void constructAttributedModificationsString(String modificationsString) {
            attributedModificationsString = new AttributedString(modificationsString);

            if (!modificationsString.equals("0")) {
                String mods = modificationsString.substring(modificationsString.indexOf(IdentificationsTableFormat.MODS_OPEN) + IdentificationsTableFormat.MODS_OPEN.length(), modificationsString.indexOf(IdentificationsTableFormat.MODS_CLOSE));
                //split modifications
                String[] splits = mods.split(IdentificationsTableFormat.MODS_DELIMITER);

                int index = 0;
                for (String modification : splits) {
                    index = modificationsString.indexOf(modification, index);
                    if (modificationColors.containsKey(modification)) {
                        for (int i = modificationsString.indexOf(modification, index); i < modificationsString.indexOf(modification, index) + modification.length(); i++) {
                            if (modificationColors.containsKey(modification)) {
                                attributedModificationsString.addAttribute(TextAttribute.FOREGROUND, modificationColors.get(modification), i, i + 1);
                            }
                        }
                        index = modificationsString.indexOf(modification, index + modification.length());
                    }
                }
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            int offset = 0;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.drawString(attributedModificationsString.getIterator(), offset, 15);
            g2.dispose();
        }
    }
}
