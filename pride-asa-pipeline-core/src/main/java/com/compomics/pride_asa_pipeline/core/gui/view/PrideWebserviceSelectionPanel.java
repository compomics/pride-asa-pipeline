/* 
 * Copyright 2018 compomics.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.compomics.pride_asa_pipeline.core.gui.view;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JTextField;

/**
 *
 * @author Niels Hulstaert
 * @author Harald Barsnes
 */
public class PrideWebserviceSelectionPanel extends javax.swing.JPanel {

    /**
     * Creates new form ExperimentSelectionPanel
     */
    public PrideWebserviceSelectionPanel() {
        initComponents();
    }

    public JButton getProcessButton() {
        return experimentProcessButton;
    }

    public JComboBox getExperimentSelectionComboBox() {
        return experimentSelectionComboBox;
    }

    public JCheckBox getWriteResultCheckBox() {
        return writeResultCheckBox;
    }

    public JCheckBox getIncludePrideModificationsCheckBox() {
        return includePrideModificationsCheckBox;
    }

    public JTextField getProjectField() {
        return projectField;
    }

    public void setProjectField(JTextField projectField) {
        this.projectField = projectField;
    }

    
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        experimentSelectionLabel = new javax.swing.JLabel();
        experimentProcessButton = new javax.swing.JButton();
        writeResultCheckBox = new javax.swing.JCheckBox();
        includePrideModificationsCheckBox = new javax.swing.JCheckBox();
        projectLabel = new javax.swing.JLabel();
        experimentLabel = new javax.swing.JLabel();
        experimentSelectionComboBox = new javax.swing.JComboBox();
        projectField = new javax.swing.JTextField();

        setOpaque(false);
        setPreferredSize(new java.awt.Dimension(50, 20));

        experimentSelectionLabel.setText("Select a PRIDE Experiment");

        experimentProcessButton.setText("Process");
        experimentProcessButton.setMaximumSize(new java.awt.Dimension(80, 25));
        experimentProcessButton.setMinimumSize(new java.awt.Dimension(80, 25));
        experimentProcessButton.setPreferredSize(new java.awt.Dimension(80, 25));

        writeResultCheckBox.setText("Write Result to File");
        writeResultCheckBox.setToolTipText("The output folder can be set on the \"Pipeline configuration\" panel.");
        writeResultCheckBox.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);
        writeResultCheckBox.setIconTextGap(15);

        includePrideModificationsCheckBox.setText("Include PRIDE Modifications");
        includePrideModificationsCheckBox.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);
        includePrideModificationsCheckBox.setIconTextGap(15);
        includePrideModificationsCheckBox.setOpaque(false);

        projectLabel.setText("Project");

        experimentLabel.setText("Experiment");

        projectField.setText("PRD000214");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(experimentLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(experimentSelectionComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(projectLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(projectField))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(experimentSelectionLabel)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(includePrideModificationsCheckBox)
                        .addGap(10, 10, 10)
                        .addComponent(writeResultCheckBox)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 144, Short.MAX_VALUE)
                        .addComponent(experimentProcessButton, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addComponent(experimentSelectionLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(projectLabel)
                    .addComponent(projectField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(experimentLabel)
                    .addComponent(experimentSelectionComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(includePrideModificationsCheckBox)
                    .addComponent(writeResultCheckBox)
                    .addComponent(experimentProcessButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel experimentLabel;
    private javax.swing.JButton experimentProcessButton;
    private javax.swing.JComboBox experimentSelectionComboBox;
    private javax.swing.JLabel experimentSelectionLabel;
    private javax.swing.JCheckBox includePrideModificationsCheckBox;
    private javax.swing.JTextField projectField;
    private javax.swing.JLabel projectLabel;
    private javax.swing.JCheckBox writeResultCheckBox;
    // End of variables declaration//GEN-END:variables
}