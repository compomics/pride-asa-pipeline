/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pride_asa_pipeline.gui.view;

import javax.swing.*;

/**
 *
 * @author Niels Hulstaert
 */
public class ModificationsPanel extends javax.swing.JPanel {
    
    JFileChooser fileChooser;
    
    /**
     * Creates new form ModificationsPanel
     */
    public ModificationsPanel() {
        initComponents();
        fileChooser = new JFileChooser();
    }

    public JTable getModifcationsTable() {
        return modifcationsTable;
    }

    public JTextField getModAccessionTextField() {
        return modAccessionTextField;
    }

    public JComboBox getModLocationComboBox() {
        return modLocationComboBox;
    }

    public JTextField getModAverageMassShiftTextField() {
        return modAverageMassShiftTextField;
    }

    public JTextField getModMonoIsotopicMassShiftTextField() {
        return modMonoIsotopicMassShiftTextField;
    }

    public JTextField getModNameTextField() {
        return modNameTextField;
    }

    public JList getAffectedAminoAcidsList() {
        return affectedAminoAcidsList;
    }

    public JButton getAddAminoAcidButton() {
        return addAminoAcidButton;
    }

    public JList getAminoAcidsList() {
        return aminoAcidsList;
    }

    public JButton getRemoveAminoAcidButton() {
        return removeAminoAcidButton;
    }

    public JButton getAddModificationButton() {
        return addModificationButton;
    }

    public JButton getRemoveModificationButton() {
        return removeModificationButton;
    }

    public JLabel getBindingLoggingLabel() {
        return bindingLoggingLabel;
    }

    public JTextField getModAccessionValueTextField() {
        return modAccessionValueTextField;
    }

    public JButton getSaveButton() {
        return saveButton;
    }

    public JButton getImportButton() {
        return importButton;
    }

    public JFileChooser getFileChooser() {
        return fileChooser;
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

        jScrollPane1 = new javax.swing.JScrollPane();
        modifcationsTable = new javax.swing.JTable();
        editModificationPanel = new javax.swing.JPanel();
        modNameLabel = new javax.swing.JLabel();
        modNameTextField = new javax.swing.JTextField();
        modAccessionLabel = new javax.swing.JLabel();
        modAccessionTextField = new javax.swing.JTextField();
        modMonoIsotopicMassShiftLabel = new javax.swing.JLabel();
        modMonoIsotopicMassShiftTextField = new javax.swing.JTextField();
        modLocationLabel = new javax.swing.JLabel();
        modLocationComboBox = new javax.swing.JComboBox();
        modAverageMassShiftLabel = new javax.swing.JLabel();
        modAverageMassShiftTextField = new javax.swing.JTextField();
        affectedAminoAcidsLabel = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        aminoAcidsList = new javax.swing.JList();
        jScrollPane2 = new javax.swing.JScrollPane();
        affectedAminoAcidsList = new javax.swing.JList();
        addAminoAcidButton = new javax.swing.JButton();
        removeAminoAcidButton = new javax.swing.JButton();
        modAccessionValueLabel = new javax.swing.JLabel();
        modAccessionValueTextField = new javax.swing.JTextField();
        bindingLoggingLabel = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        addModificationButton = new javax.swing.JButton();
        removeModificationButton = new javax.swing.JButton();
        importButton = new javax.swing.JButton();
        saveButton = new javax.swing.JButton();

        setLayout(new java.awt.GridBagLayout());

        jScrollPane1.setBorder(javax.swing.BorderFactory.createTitledBorder("Modifications table"));
        jScrollPane1.setPreferredSize(new java.awt.Dimension(25, 25));

        modifcationsTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        modifcationsTable.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jScrollPane1.setViewportView(modifcationsTable);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.4;
        add(jScrollPane1, gridBagConstraints);

        editModificationPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Modification details"));
        editModificationPanel.setMinimumSize(new java.awt.Dimension(100, 100));
        editModificationPanel.setPreferredSize(new java.awt.Dimension(300, 300));

        modNameLabel.setText("name");

        modNameTextField.setMinimumSize(new java.awt.Dimension(80, 20));
        modNameTextField.setPreferredSize(new java.awt.Dimension(300, 30));

        modAccessionLabel.setText("accession");

        modAccessionTextField.setMinimumSize(new java.awt.Dimension(80, 20));
        modAccessionTextField.setPreferredSize(new java.awt.Dimension(300, 30));

        modMonoIsotopicMassShiftLabel.setText("monoisotopic mass shift");

        modMonoIsotopicMassShiftTextField.setMinimumSize(new java.awt.Dimension(80, 20));
        modMonoIsotopicMassShiftTextField.setPreferredSize(new java.awt.Dimension(300, 30));

        modLocationLabel.setText("location");

        modLocationComboBox.setMinimumSize(new java.awt.Dimension(80, 20));
        modLocationComboBox.setPreferredSize(new java.awt.Dimension(300, 30));

        modAverageMassShiftLabel.setText("average mass shift");

        modAverageMassShiftTextField.setMinimumSize(new java.awt.Dimension(80, 20));
        modAverageMassShiftTextField.setPreferredSize(new java.awt.Dimension(300, 30));

        affectedAminoAcidsLabel.setText("affected amino acids");

        jScrollPane3.setMaximumSize(new java.awt.Dimension(0, 0));
        jScrollPane3.setMinimumSize(new java.awt.Dimension(20, 20));
        jScrollPane3.setPreferredSize(new java.awt.Dimension(20, 20));

        jScrollPane3.setViewportView(aminoAcidsList);

        jScrollPane2.setMaximumSize(new java.awt.Dimension(0, 0));
        jScrollPane2.setMinimumSize(new java.awt.Dimension(20, 20));
        jScrollPane2.setPreferredSize(new java.awt.Dimension(20, 20));

        jScrollPane2.setViewportView(affectedAminoAcidsList);

        addAminoAcidButton.setText(">>>");

        removeAminoAcidButton.setText("<<<");

        modAccessionValueLabel.setText("accession value");

        modAccessionValueTextField.setMinimumSize(new java.awt.Dimension(80, 20));
        modAccessionValueTextField.setPreferredSize(new java.awt.Dimension(300, 30));

        bindingLoggingLabel.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        bindingLoggingLabel.setForeground(new java.awt.Color(255, 0, 0));
        bindingLoggingLabel.setMinimumSize(new java.awt.Dimension(80, 25));
        bindingLoggingLabel.setPreferredSize(new java.awt.Dimension(80, 25));

        javax.swing.GroupLayout editModificationPanelLayout = new javax.swing.GroupLayout(editModificationPanel);
        editModificationPanel.setLayout(editModificationPanelLayout);
        editModificationPanelLayout.setHorizontalGroup(
            editModificationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(editModificationPanelLayout.createSequentialGroup()
                .addGroup(editModificationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(modLocationLabel)
                    .addComponent(modAverageMassShiftLabel)
                    .addComponent(modMonoIsotopicMassShiftLabel)
                    .addComponent(modAccessionValueLabel)
                    .addComponent(modAccessionLabel)
                    .addComponent(affectedAminoAcidsLabel))
                .addGap(48, 48, 48)
                .addGroup(editModificationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addGroup(editModificationPanelLayout.createSequentialGroup()
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(editModificationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(addAminoAcidButton, javax.swing.GroupLayout.DEFAULT_SIZE, 86, Short.MAX_VALUE)
                            .addComponent(removeAminoAcidButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(128, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, editModificationPanelLayout.createSequentialGroup()
                        .addGroup(editModificationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(modMonoIsotopicMassShiftTextField, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(modAccessionValueTextField, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(modNameTextField, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(modAccessionTextField, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(modAverageMassShiftTextField, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(modLocationComboBox, javax.swing.GroupLayout.Alignment.LEADING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(0, 0, Short.MAX_VALUE))))
            .addGroup(editModificationPanelLayout.createSequentialGroup()
                .addGroup(editModificationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(bindingLoggingLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 387, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(modNameLabel))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        editModificationPanelLayout.setVerticalGroup(
            editModificationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(editModificationPanelLayout.createSequentialGroup()
                .addComponent(bindingLoggingLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(editModificationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(modNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(modNameLabel))
                .addGap(11, 11, 11)
                .addGroup(editModificationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(modAccessionTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(modAccessionLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(editModificationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(modAccessionValueTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(modAccessionValueLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(editModificationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(modMonoIsotopicMassShiftTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(modMonoIsotopicMassShiftLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(editModificationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(modAverageMassShiftTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(modAverageMassShiftLabel))
                .addGap(14, 14, 14)
                .addGroup(editModificationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(modLocationLabel)
                    .addComponent(modLocationComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(editModificationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(editModificationPanelLayout.createSequentialGroup()
                        .addGap(61, 61, 61)
                        .addComponent(addAminoAcidButton)
                        .addGap(37, 37, 37)
                        .addComponent(removeAminoAcidButton))
                    .addComponent(affectedAminoAcidsLabel)
                    .addGroup(editModificationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 200, Short.MAX_VALUE)
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap(0, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.6;
        add(editModificationPanel, gridBagConstraints);

        addModificationButton.setText("add");
        addModificationButton.setToolTipText("Click to add a modification.");
        addModificationButton.setMaximumSize(new java.awt.Dimension(80, 25));
        addModificationButton.setMinimumSize(new java.awt.Dimension(80, 25));
        addModificationButton.setPreferredSize(new java.awt.Dimension(80, 25));

        removeModificationButton.setText("remove");
        removeModificationButton.setToolTipText("Click to delete the selected modification");
        removeModificationButton.setMaximumSize(new java.awt.Dimension(80, 25));
        removeModificationButton.setMinimumSize(new java.awt.Dimension(80, 25));
        removeModificationButton.setPreferredSize(new java.awt.Dimension(80, 25));

        importButton.setText("import");
        importButton.setToolTipText("Click to import a modification file.");
        importButton.setMaximumSize(new java.awt.Dimension(80, 25));
        importButton.setMinimumSize(new java.awt.Dimension(80, 25));
        importButton.setPreferredSize(new java.awt.Dimension(80, 25));

        saveButton.setText("save");
        saveButton.setToolTipText("Click to save the modifications to the the modifications file.");
        saveButton.setMaximumSize(new java.awt.Dimension(80, 25));
        saveButton.setMinimumSize(new java.awt.Dimension(80, 25));
        saveButton.setPreferredSize(new java.awt.Dimension(80, 25));

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(saveButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(importButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 344, Short.MAX_VALUE)
                .addComponent(addModificationButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(removeModificationButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(saveButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(importButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(addModificationButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(removeModificationButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.weightx = 1.0;
        add(jPanel3, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addAminoAcidButton;
    private javax.swing.JButton addModificationButton;
    private javax.swing.JLabel affectedAminoAcidsLabel;
    private javax.swing.JList affectedAminoAcidsList;
    private javax.swing.JList aminoAcidsList;
    private javax.swing.JLabel bindingLoggingLabel;
    private javax.swing.JPanel editModificationPanel;
    private javax.swing.JButton importButton;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JLabel modAccessionLabel;
    private javax.swing.JTextField modAccessionTextField;
    private javax.swing.JLabel modAccessionValueLabel;
    private javax.swing.JTextField modAccessionValueTextField;
    private javax.swing.JLabel modAverageMassShiftLabel;
    private javax.swing.JTextField modAverageMassShiftTextField;
    private javax.swing.JComboBox modLocationComboBox;
    private javax.swing.JLabel modLocationLabel;
    private javax.swing.JLabel modMonoIsotopicMassShiftLabel;
    private javax.swing.JTextField modMonoIsotopicMassShiftTextField;
    private javax.swing.JLabel modNameLabel;
    private javax.swing.JTextField modNameTextField;
    private javax.swing.JTable modifcationsTable;
    private javax.swing.JButton removeAminoAcidButton;
    private javax.swing.JButton removeModificationButton;
    private javax.swing.JButton saveButton;
    // End of variables declaration//GEN-END:variables
}