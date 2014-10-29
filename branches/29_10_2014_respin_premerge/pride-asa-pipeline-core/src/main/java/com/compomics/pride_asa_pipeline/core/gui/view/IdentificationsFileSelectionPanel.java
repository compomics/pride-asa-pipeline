package com.compomics.pride_asa_pipeline.core.gui.view;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;

/**
 *
 * @author Niels Hulstaert
 * @author Harald Barsnes
 */
public class IdentificationsFileSelectionPanel extends javax.swing.JPanel {

    private JFileChooser fileChooser;

    /**
     * Creates new form FileSelectionPanel
     */
    public IdentificationsFileSelectionPanel() {
        initComponents();
        fileChooser = new JFileChooser();
    }

    public JFileChooser getFileChooser() {
        return fileChooser;
    }

    public JLabel getFileNameLabel() {
        return fileNameLabel;
    }

    public JButton getProcessButton() {
        return processButton;
    }

    public JButton getSelectFileButton() {
        return selectFileButton;
    }

    public JCheckBox getIncludeIdentificationsFileModificationsCheckBox() {
        return includeIdentificationsFileModificationsCheckBox;
    }     

    public JCheckBox getWriteResultCheckBox() {
        return writeResultCheckBox;
    }        

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        fileSelectionLabel = new javax.swing.JLabel();
        processButton = new javax.swing.JButton();
        fileNameLabel = new javax.swing.JLabel();
        selectFileButton = new javax.swing.JButton();
        includeIdentificationsFileModificationsCheckBox = new javax.swing.JCheckBox();
        writeResultCheckBox = new javax.swing.JCheckBox();

        setOpaque(false);
        setPreferredSize(new java.awt.Dimension(50, 50));

        fileSelectionLabel.setText("Select an identifications File");

        processButton.setText("Process");
        processButton.setMaximumSize(new java.awt.Dimension(80, 25));
        processButton.setMinimumSize(new java.awt.Dimension(80, 25));
        processButton.setPreferredSize(new java.awt.Dimension(80, 25));

        fileNameLabel.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(204, 204, 204), 1, true));

        selectFileButton.setText("Browse");
        selectFileButton.setMaximumSize(new java.awt.Dimension(80, 25));
        selectFileButton.setMinimumSize(new java.awt.Dimension(80, 25));
        selectFileButton.setPreferredSize(new java.awt.Dimension(80, 25));

        includeIdentificationsFileModificationsCheckBox.setText("Include identifications file Modifications");
        includeIdentificationsFileModificationsCheckBox.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);
        includeIdentificationsFileModificationsCheckBox.setIconTextGap(15);

        writeResultCheckBox.setText("Write Result to File");
        writeResultCheckBox.setToolTipText("The output folder can be set on the \"Pipeline configuration\" panel.");
        writeResultCheckBox.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);
        writeResultCheckBox.setIconTextGap(15);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addComponent(fileSelectionLabel)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(fileNameLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(selectFileButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(0, 26, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(writeResultCheckBox)
                            .addComponent(includeIdentificationsFileModificationsCheckBox)
                            .addComponent(processButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap())
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {processButton, selectFileButton});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(selectFileButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(fileSelectionLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(fileNameLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 23, Short.MAX_VALUE)
                .addComponent(includeIdentificationsFileModificationsCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(writeResultCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(processButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel fileNameLabel;
    private javax.swing.JLabel fileSelectionLabel;
    private javax.swing.JCheckBox includeIdentificationsFileModificationsCheckBox;
    private javax.swing.JButton processButton;
    private javax.swing.JButton selectFileButton;
    private javax.swing.JCheckBox writeResultCheckBox;
    // End of variables declaration//GEN-END:variables
}
