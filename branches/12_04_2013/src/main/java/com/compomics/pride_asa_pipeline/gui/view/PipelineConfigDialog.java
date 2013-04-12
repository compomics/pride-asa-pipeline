
package com.compomics.pride_asa_pipeline.gui.view;

import java.awt.Color;
import javax.swing.JButton;
import javax.swing.JTable;

/**
 * A dialog for editing the piple line configuration.
 * 
 * @author Harald Barsnes
 */
public class PipelineConfigDialog extends javax.swing.JDialog {

    /**
     * Creates a new PipelineConfigDialog.
     * 
     * @param parent
     * @param modal  
     */
    public PipelineConfigDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        this.getContentPane().setBackground(Color.WHITE);
        pipelineParamsTableScrollPane.getViewport().setOpaque(false);
        pipelineParamsTable.getTableHeader().setReorderingAllowed(false);
        pipelineParamsTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        setLocationRelativeTo(parent);
    }

    public JTable getPipelineParamsTable() {
        return pipelineParamsTable;
    }

    public JButton getResetButton() {
        return resetButton;
    }

    public JButton getSaveButton() {
        return saveButton;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        configPanel = new javax.swing.JPanel();
        pipelineParamsTableScrollPane = new javax.swing.JScrollPane();
        pipelineParamsTable = new javax.swing.JTable();
        resetButton = new javax.swing.JButton();
        saveButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Pipeline Configuration");

        configPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Parameters"));
        configPanel.setOpaque(false);

        pipelineParamsTableScrollPane.setOpaque(false);

        pipelineParamsTable.setModel(new javax.swing.table.DefaultTableModel(
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
        pipelineParamsTable.setOpaque(false);
        pipelineParamsTableScrollPane.setViewportView(pipelineParamsTable);

        resetButton.setText("Reset");
        resetButton.setToolTipText("Click to reset to the default pipeline parameters");
        resetButton.setMaximumSize(new java.awt.Dimension(80, 25));
        resetButton.setMinimumSize(new java.awt.Dimension(80, 25));
        resetButton.setPreferredSize(new java.awt.Dimension(80, 25));

        saveButton.setText("Save");
        saveButton.setToolTipText("Click to save the current pipeline parameters to file");
        saveButton.setMaximumSize(new java.awt.Dimension(80, 25));
        saveButton.setMinimumSize(new java.awt.Dimension(80, 25));
        saveButton.setPreferredSize(new java.awt.Dimension(80, 25));

        javax.swing.GroupLayout configPanelLayout = new javax.swing.GroupLayout(configPanel);
        configPanel.setLayout(configPanelLayout);
        configPanelLayout.setHorizontalGroup(
            configPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(configPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(configPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(configPanelLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(resetButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(saveButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(pipelineParamsTableScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 657, Short.MAX_VALUE))
                .addContainerGap())
        );

        configPanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {resetButton, saveButton});

        configPanelLayout.setVerticalGroup(
            configPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(configPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pipelineParamsTableScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 416, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(configPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(saveButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(resetButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(configPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(configPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel configPanel;
    private javax.swing.JTable pipelineParamsTable;
    private javax.swing.JScrollPane pipelineParamsTableScrollPane;
    private javax.swing.JButton resetButton;
    private javax.swing.JButton saveButton;
    // End of variables declaration//GEN-END:variables
}
