package com.compomics.pride_asa_pipeline.gui.view;

import javax.swing.JPanel;

/**
 *
 * @author Niels Hulstaert
 * @author Harald Barsnes
 */
public class SummaryPanel extends javax.swing.JPanel {

    /**
     * Creates new form SummaryPanel
     */
    public SummaryPanel() {
        initComponents();
    }

    public JPanel getIdentificationsChartParentPanel() {
        return identificationsChartParentPanel;
    }

    public JPanel getModificationsChartParentPanel() {
        return modificationsChartParentPanel;
    }

    public JPanel getScoresChartParentPanel() {
        return scoresChartParentPanel;
    }

    public JPanel getPrecursorMassDeltaChartParentPanel() {
        return precursorMassDeltaChartParentPanel;
    }

    public JPanel getFragmentIonMassDeltaChartParentPanel() {
        return fragmentIonMassDeltaChartParentPanel;
    }

    public JPanel getbIonCoverageChartParentPanel() {
        return bIonCoverageChartParentPanel;
    }

    public JPanel getyIonCoverageChartParentPanel() {
        return yIonCoverageChartParentPanel;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        identificationsChartParentPanel = new javax.swing.JPanel();
        modificationsChartParentPanel = new javax.swing.JPanel();
        generalSummaryPanel = new javax.swing.JPanel();
        precursorMassDeltaChartParentPanel = new javax.swing.JPanel();
        scoresChartParentPanel = new javax.swing.JPanel();
        fragmentIonMassDeltaChartParentPanel = new javax.swing.JPanel();
        ionCoveragesChartPanel = new javax.swing.JPanel();
        bIonCoverageChartParentPanel = new javax.swing.JPanel();
        yIonCoverageChartParentPanel = new javax.swing.JPanel();

        setBackground(new java.awt.Color(255, 255, 255));
        setOpaque(false);
        setPreferredSize(new java.awt.Dimension(400, 200));
        java.awt.GridBagLayout layout = new java.awt.GridBagLayout();
        layout.columnWidths = new int[] {0, 5, 0};
        layout.rowHeights = new int[] {0, 5, 0};
        setLayout(layout);

        identificationsChartParentPanel.setBackground(new java.awt.Color(255, 255, 255));
        identificationsChartParentPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Identifications"));
        identificationsChartParentPanel.setOpaque(false);
        identificationsChartParentPanel.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.33;
        gridBagConstraints.weighty = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        add(identificationsChartParentPanel, gridBagConstraints);

        modificationsChartParentPanel.setBackground(new java.awt.Color(255, 255, 255));
        modificationsChartParentPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Modifications"));
        modificationsChartParentPanel.setOpaque(false);
        modificationsChartParentPanel.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.33;
        gridBagConstraints.weighty = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 6, 0);
        add(modificationsChartParentPanel, gridBagConstraints);

        generalSummaryPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("General"));
        generalSummaryPanel.setOpaque(false);
        generalSummaryPanel.setPreferredSize(new java.awt.Dimension(12, 23));
        generalSummaryPanel.setLayout(new java.awt.GridBagLayout());

        precursorMassDeltaChartParentPanel.setOpaque(false);
        precursorMassDeltaChartParentPanel.setPreferredSize(new java.awt.Dimension(20, 20));
        precursorMassDeltaChartParentPanel.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.weighty = 0.5;
        generalSummaryPanel.add(precursorMassDeltaChartParentPanel, gridBagConstraints);

        scoresChartParentPanel.setOpaque(false);
        scoresChartParentPanel.setPreferredSize(new java.awt.Dimension(20, 20));
        scoresChartParentPanel.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.weighty = 0.5;
        generalSummaryPanel.add(scoresChartParentPanel, gridBagConstraints);

        fragmentIonMassDeltaChartParentPanel.setOpaque(false);
        fragmentIonMassDeltaChartParentPanel.setPreferredSize(new java.awt.Dimension(20, 20));
        fragmentIonMassDeltaChartParentPanel.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.weighty = 0.5;
        generalSummaryPanel.add(fragmentIonMassDeltaChartParentPanel, gridBagConstraints);

        ionCoveragesChartPanel.setOpaque(false);
        ionCoveragesChartPanel.setPreferredSize(new java.awt.Dimension(20, 20));
        ionCoveragesChartPanel.setLayout(new java.awt.GridBagLayout());

        bIonCoverageChartParentPanel.setOpaque(false);
        bIonCoverageChartParentPanel.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.5;
        ionCoveragesChartPanel.add(bIonCoverageChartParentPanel, gridBagConstraints);

        yIonCoverageChartParentPanel.setOpaque(false);
        yIonCoverageChartParentPanel.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.5;
        ionCoveragesChartPanel.add(yIonCoverageChartParentPanel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.weighty = 0.5;
        generalSummaryPanel.add(ionCoveragesChartPanel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.66;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 6, 6);
        add(generalSummaryPanel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel bIonCoverageChartParentPanel;
    private javax.swing.JPanel fragmentIonMassDeltaChartParentPanel;
    private javax.swing.JPanel generalSummaryPanel;
    private javax.swing.JPanel identificationsChartParentPanel;
    private javax.swing.JPanel ionCoveragesChartPanel;
    private javax.swing.JPanel modificationsChartParentPanel;
    private javax.swing.JPanel precursorMassDeltaChartParentPanel;
    private javax.swing.JPanel scoresChartParentPanel;
    private javax.swing.JPanel yIonCoverageChartParentPanel;
    // End of variables declaration//GEN-END:variables
}
