/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pride_asa_pipeline.gui.view;

import javax.swing.JPanel;
import javax.swing.JTable;

/**
 *
 * @author niels
 */
public class PipelineResultPanel extends javax.swing.JPanel {

    /**
     * Creates new form ExperimentDetailPanel
     */
    public PipelineResultPanel() {
        initComponents();
    }

    public JTable getIdentificationsTable() {
        return identificationsTable;
    }

    public JPanel getIdentificationDetailPanel() {
        return identificationDetailPanel;
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

        identificationsTablePanel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        identificationsTable = new javax.swing.JTable();
        identificationDetailPanel = new javax.swing.JPanel();

        setMinimumSize(new java.awt.Dimension(520, 520));
        setPreferredSize(new java.awt.Dimension(520, 520));
        setLayout(new java.awt.GridBagLayout());

        identificationsTablePanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Identifications"));
        identificationsTablePanel.setMinimumSize(new java.awt.Dimension(20, 20));
        identificationsTablePanel.setPreferredSize(new java.awt.Dimension(20, 20));

        identificationsTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane1.setViewportView(identificationsTable);

        org.jdesktop.layout.GroupLayout identificationsTablePanelLayout = new org.jdesktop.layout.GroupLayout(identificationsTablePanel);
        identificationsTablePanel.setLayout(identificationsTablePanelLayout);
        identificationsTablePanelLayout.setHorizontalGroup(
            identificationsTablePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 508, Short.MAX_VALUE)
        );
        identificationsTablePanelLayout.setVerticalGroup(
            identificationsTablePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 141, Short.MAX_VALUE)
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.3;
        add(identificationsTablePanel, gridBagConstraints);

        identificationDetailPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Identification detail"));
        identificationDetailPanel.setMinimumSize(new java.awt.Dimension(20, 20));
        identificationDetailPanel.setPreferredSize(new java.awt.Dimension(20, 20));
        identificationDetailPanel.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.7;
        add(identificationDetailPanel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel identificationDetailPanel;
    private javax.swing.JTable identificationsTable;
    private javax.swing.JPanel identificationsTablePanel;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables
}
