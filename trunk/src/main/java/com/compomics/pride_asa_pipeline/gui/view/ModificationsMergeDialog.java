/*
 */
package com.compomics.pride_asa_pipeline.gui.view;

import java.awt.Color;
import java.awt.Frame;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JTextField;

/**
 *
 * @author niels
 */
public class ModificationsMergeDialog extends javax.swing.JDialog {

    /**
     * Creates new form ModificationsMergeDialog
     */
    public ModificationsMergeDialog(Frame parent) {
        super(parent);
        this.getContentPane().setBackground(Color.WHITE);
        initComponents();
    }

    public JButton getAddPrideModificationButton() {
        return addPrideModificationButton;
    }

    public void setAddPrideModificationButton(JButton addPrideModificationButton) {
        this.addPrideModificationButton = addPrideModificationButton;
    }

    public JButton getCancelButton() {
        return cancelButton;
    }

    public void setCancelButton(JButton cancelButton) {
        this.cancelButton = cancelButton;
    }

    public JList getPipelineModificationsList() {
        return pipelineModificationsList;
    }

    public void setPipelineModificationsList(JList pipelineModificationsList) {
        this.pipelineModificationsList = pipelineModificationsList;
    }

    public JList getPrideModificationsList() {
        return prideModificationsList;
    }

    public void setPrideModificationsList(JList prideModificationsList) {
        this.prideModificationsList = prideModificationsList;
    }

    public JButton getProceedButton() {
        return proceedButton;
    }

    public void setProceedButton(JButton proceedButton) {
        this.proceedButton = proceedButton;
    }

    public JButton getRemovePrideModificationButton() {
        return removeModificationButton;
    }

    public void setRemoveModificationButton(JButton removeModificationButton) {
        this.removeModificationButton = removeModificationButton;
    }

    public JTextField getModAccessionTextField() {
        return modAccessionTextField;
    }

    public JTextField getModAccessionValueTextField() {
        return modAccessionValueTextField;
    }

    public JTextField getModAverageModMassShiftTextField() {
        return modAverageModMassShiftTextField;
    }

    public JTextField getModMonoMassShiftTextField() {
        return modMonoMassShiftTextField;
    }

    public JTextField getPrideModAccessionTextField() {
        return prideModAccessionTextField;
    }

    public JTextField getPrideModAccessionValueTextField() {
        return prideModAccessionValueTextField;
    }

    public JTextField getPrideModAverageModMassShiftTextField() {
        return prideModAverageModMassShiftTextField;
    }

    public JTextField getPrideModMonoMassShiftTextField() {
        return prideModMonoMassShiftTextField;
    }
        
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        prideModificationsList = new javax.swing.JList();
        jScrollPane2 = new javax.swing.JScrollPane();
        pipelineModificationsList = new javax.swing.JList();
        cancelButton = new javax.swing.JButton();
        proceedButton = new javax.swing.JButton();
        prideModAccessionLabel = new javax.swing.JLabel();
        prideModAccessionValueLabel = new javax.swing.JLabel();
        modAccessionLabel = new javax.swing.JLabel();
        modAccessionValueLabel = new javax.swing.JLabel();
        modMonoIsotopicMassShiftLabel = new javax.swing.JLabel();
        prideModMonoIsotopicMassShiftLabel = new javax.swing.JLabel();
        modAverageMassShiftLabel = new javax.swing.JLabel();
        prideModAverageMassShiftLabel = new javax.swing.JLabel();
        modMonoMassShiftTextField = new javax.swing.JTextField();
        prideModAccessionTextField = new javax.swing.JTextField();
        prideModAccessionValueTextField = new javax.swing.JTextField();
        prideModAverageModMassShiftTextField = new javax.swing.JTextField();
        modAccessionTextField = new javax.swing.JTextField();
        modAccessionValueTextField = new javax.swing.JTextField();
        prideModMonoMassShiftTextField = new javax.swing.JTextField();
        modAverageModMassShiftTextField = new javax.swing.JTextField();
        jTextArea1 = new javax.swing.JTextArea();
        jPanel1 = new javax.swing.JPanel();
        addPrideModificationButton = new javax.swing.JButton();
        removeModificationButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Modifications conflict(s)");
        setResizable(false);

        jScrollPane1.setBorder(javax.swing.BorderFactory.createTitledBorder("Pride modifications"));

        jScrollPane1.setViewportView(prideModificationsList);

        jScrollPane2.setBorder(javax.swing.BorderFactory.createTitledBorder("Pipeline modifications"));

        jScrollPane2.setViewportView(pipelineModificationsList);

        cancelButton.setText("cancel");
        cancelButton.setMaximumSize(new java.awt.Dimension(80, 25));
        cancelButton.setMinimumSize(new java.awt.Dimension(80, 25));
        cancelButton.setPreferredSize(new java.awt.Dimension(80, 25));

        proceedButton.setText("proceed");
        proceedButton.setMaximumSize(new java.awt.Dimension(80, 25));
        proceedButton.setMinimumSize(new java.awt.Dimension(80, 25));
        proceedButton.setPreferredSize(new java.awt.Dimension(80, 25));

        prideModAccessionLabel.setText("accession");

        prideModAccessionValueLabel.setText("value");

        modAccessionLabel.setText("accession");

        modAccessionValueLabel.setText("value");

        modMonoIsotopicMassShiftLabel.setText("monoisotopic mass shift");

        prideModMonoIsotopicMassShiftLabel.setText("monoisotopic mass shift");

        modAverageMassShiftLabel.setText("average mass shift");

        prideModAverageMassShiftLabel.setText("average mass shift");

        modMonoMassShiftTextField.setEditable(false);
        modMonoMassShiftTextField.setMinimumSize(new java.awt.Dimension(80, 20));
        modMonoMassShiftTextField.setOpaque(false);
        modMonoMassShiftTextField.setPreferredSize(new java.awt.Dimension(300, 30));

        prideModAccessionTextField.setEditable(false);
        prideModAccessionTextField.setMinimumSize(new java.awt.Dimension(80, 20));
        prideModAccessionTextField.setOpaque(false);
        prideModAccessionTextField.setPreferredSize(new java.awt.Dimension(300, 30));

        prideModAccessionValueTextField.setEditable(false);
        prideModAccessionValueTextField.setMinimumSize(new java.awt.Dimension(80, 20));
        prideModAccessionValueTextField.setOpaque(false);
        prideModAccessionValueTextField.setPreferredSize(new java.awt.Dimension(300, 30));

        prideModAverageModMassShiftTextField.setEditable(false);
        prideModAverageModMassShiftTextField.setMinimumSize(new java.awt.Dimension(80, 20));
        prideModAverageModMassShiftTextField.setOpaque(false);
        prideModAverageModMassShiftTextField.setPreferredSize(new java.awt.Dimension(300, 30));

        modAccessionTextField.setEditable(false);
        modAccessionTextField.setMinimumSize(new java.awt.Dimension(80, 20));
        modAccessionTextField.setOpaque(false);
        modAccessionTextField.setPreferredSize(new java.awt.Dimension(300, 30));

        modAccessionValueTextField.setEditable(false);
        modAccessionValueTextField.setMinimumSize(new java.awt.Dimension(80, 20));
        modAccessionValueTextField.setOpaque(false);
        modAccessionValueTextField.setPreferredSize(new java.awt.Dimension(300, 30));

        prideModMonoMassShiftTextField.setEditable(false);
        prideModMonoMassShiftTextField.setMinimumSize(new java.awt.Dimension(80, 20));
        prideModMonoMassShiftTextField.setOpaque(false);
        prideModMonoMassShiftTextField.setPreferredSize(new java.awt.Dimension(300, 30));

        modAverageModMassShiftTextField.setEditable(false);
        modAverageModMassShiftTextField.setMinimumSize(new java.awt.Dimension(80, 20));
        modAverageModMassShiftTextField.setOpaque(false);
        modAverageModMassShiftTextField.setPreferredSize(new java.awt.Dimension(300, 30));

        jTextArea1.setEditable(false);
        jTextArea1.setColumns(20);
        jTextArea1.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        jTextArea1.setRows(3);
        jTextArea1.setText("Some of the modifications found in pride for this experiment have equal masses compared to the fixed set of pipeline modifications.\nPlease resolve these conflicting modifications by adding or removing pride modifications to the pipeline modifications.\nAdding or removing fixed pipeline modifications can be done in the \"Pipeline modifications\" section.");

        jPanel1.setOpaque(false);

        addPrideModificationButton.setText(">>>");
        addPrideModificationButton.setMaximumSize(new java.awt.Dimension(80, 25));
        addPrideModificationButton.setMinimumSize(new java.awt.Dimension(80, 25));
        addPrideModificationButton.setPreferredSize(new java.awt.Dimension(80, 25));
        addPrideModificationButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addPrideModificationButtonActionPerformed(evt);
            }
        });

        removeModificationButton.setText("<<<");
        removeModificationButton.setToolTipText("Only pride modifications can be removed from the pipeline modifications.");
        removeModificationButton.setMaximumSize(new java.awt.Dimension(80, 25));
        removeModificationButton.setMinimumSize(new java.awt.Dimension(80, 25));
        removeModificationButton.setPreferredSize(new java.awt.Dimension(80, 25));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(removeModificationButton, javax.swing.GroupLayout.PREFERRED_SIZE, 86, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(addPrideModificationButton, javax.swing.GroupLayout.PREFERRED_SIZE, 86, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(addPrideModificationButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(removeModificationButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(cancelButton, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(proceedButton, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(layout.createSequentialGroup()
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                    .addComponent(prideModMonoIsotopicMassShiftLabel)
                                                    .addComponent(prideModAverageMassShiftLabel))
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                    .addComponent(prideModAverageModMassShiftTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 1, Short.MAX_VALUE)
                                                    .addComponent(prideModMonoMassShiftTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 1, Short.MAX_VALUE)))
                                            .addGroup(layout.createSequentialGroup()
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                    .addComponent(prideModAccessionLabel)
                                                    .addComponent(prideModAccessionValueLabel))
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                    .addComponent(prideModAccessionValueTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 1, Short.MAX_VALUE)
                                                    .addGroup(layout.createSequentialGroup()
                                                        .addComponent(prideModAccessionTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 261, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addGap(0, 0, Short.MAX_VALUE)))))
                                        .addGap(118, 118, 118))
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                        .addComponent(jScrollPane1)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 311, Short.MAX_VALUE)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(modAccessionLabel)
                                            .addComponent(modAccessionValueLabel))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(modAccessionValueTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 1, Short.MAX_VALUE)
                                            .addComponent(modAccessionTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 1, Short.MAX_VALUE)))
                                    .addGroup(layout.createSequentialGroup()
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(modMonoIsotopicMassShiftLabel)
                                            .addComponent(modAverageMassShiftLabel))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(modAverageModMassShiftTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 1, Short.MAX_VALUE)
                                            .addComponent(modMonoMassShiftTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 1, Short.MAX_VALUE)))))
                            .addComponent(jTextArea1))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTextArea1, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 197, Short.MAX_VALUE)
                    .addComponent(jScrollPane2)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(prideModAccessionLabel)
                    .addComponent(prideModAccessionTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(modAccessionLabel)
                    .addComponent(modAccessionTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(prideModAccessionValueLabel)
                    .addComponent(prideModAccessionValueTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(modAccessionValueLabel)
                    .addComponent(modAccessionValueTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(prideModMonoIsotopicMassShiftLabel)
                    .addComponent(prideModMonoMassShiftTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(modMonoIsotopicMassShiftLabel)
                    .addComponent(modMonoMassShiftTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(prideModAverageMassShiftLabel)
                    .addComponent(prideModAverageModMassShiftTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(modAverageMassShiftLabel)
                    .addComponent(modAverageModMassShiftTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(proceedButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cancelButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void addPrideModificationButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addPrideModificationButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_addPrideModificationButtonActionPerformed
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addPrideModificationButton;
    private javax.swing.JButton cancelButton;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JLabel modAccessionLabel;
    private javax.swing.JTextField modAccessionTextField;
    private javax.swing.JLabel modAccessionValueLabel;
    private javax.swing.JTextField modAccessionValueTextField;
    private javax.swing.JLabel modAverageMassShiftLabel;
    private javax.swing.JTextField modAverageModMassShiftTextField;
    private javax.swing.JLabel modMonoIsotopicMassShiftLabel;
    private javax.swing.JTextField modMonoMassShiftTextField;
    private javax.swing.JList pipelineModificationsList;
    private javax.swing.JLabel prideModAccessionLabel;
    private javax.swing.JTextField prideModAccessionTextField;
    private javax.swing.JLabel prideModAccessionValueLabel;
    private javax.swing.JTextField prideModAccessionValueTextField;
    private javax.swing.JLabel prideModAverageMassShiftLabel;
    private javax.swing.JTextField prideModAverageModMassShiftTextField;
    private javax.swing.JLabel prideModMonoIsotopicMassShiftLabel;
    private javax.swing.JTextField prideModMonoMassShiftTextField;
    private javax.swing.JList prideModificationsList;
    private javax.swing.JButton proceedButton;
    private javax.swing.JButton removeModificationButton;
    // End of variables declaration//GEN-END:variables
}