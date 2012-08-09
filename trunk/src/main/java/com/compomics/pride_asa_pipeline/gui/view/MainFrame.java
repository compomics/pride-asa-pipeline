/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pride_asa_pipeline.gui.view;

import javax.swing.JMenuItem;
import javax.swing.JPanel;

/**
 *
 * @author Niels Hulstaert
 */
public class MainFrame extends javax.swing.JFrame {

    /**
     * Creates new form MainFrame
     */
    public MainFrame() {
        initComponents();
    }

    public JPanel getExperimentSelectionParentPanel() {
        return experimentSelectionParentPanel;
    }

    public JPanel getPipelinePanel() {
        return pipelinePanel;
    }

    public JPanel getModificationsParentPanel() {
        return modificationsParentPanel;
    }

    public JMenuItem getModificationsViewMenuItem() {
        return modificationsViewMenuItem;
    }

    public JMenuItem getPipelineParamsViewMenuItem() {
        return pipelineParamsViewMenuItem;
    }

    public JMenuItem getPipelineViewMenuItem() {
        return pipelineViewMenuItem;
    }

    public JPanel getIdentificationsParentPanel() {
        return identificationsParentPanel;
    }

    public JPanel getPipelineParamsParentPanel() {
        return pipelineParamsParentPanel;
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

        pipelinePanel = new javax.swing.JPanel();
        experimentSelectionParentPanel = new javax.swing.JPanel();
        pipelineResultTabbedPane = new javax.swing.JTabbedPane();
        summaryParentPanel = new javax.swing.JPanel();
        identificationsParentPanel = new javax.swing.JPanel();
        modificationsParentPanel = new javax.swing.JPanel();
        pipelineParamsParentPanel = new javax.swing.JPanel();
        menuBar = new javax.swing.JMenuBar();
        viewMenu = new javax.swing.JMenu();
        pipelineViewMenuItem = new javax.swing.JMenuItem();
        modificationsViewMenuItem = new javax.swing.JMenuItem();
        pipelineParamsViewMenuItem = new javax.swing.JMenuItem();
        helpMenu = new javax.swing.JMenu();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setPreferredSize(new java.awt.Dimension(1000, 800));
        getContentPane().setLayout(new java.awt.CardLayout());

        pipelinePanel.setLayout(new java.awt.GridBagLayout());

        experimentSelectionParentPanel.setMinimumSize(new java.awt.Dimension(20, 20));
        experimentSelectionParentPanel.setPreferredSize(new java.awt.Dimension(20, 20));
        experimentSelectionParentPanel.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.PAGE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.3;
        pipelinePanel.add(experimentSelectionParentPanel, gridBagConstraints);

        pipelineResultTabbedPane.setBorder(javax.swing.BorderFactory.createTitledBorder("Pipeline result"));
        pipelineResultTabbedPane.setMinimumSize(new java.awt.Dimension(20, 20));
        pipelineResultTabbedPane.setPreferredSize(new java.awt.Dimension(20, 20));

        summaryParentPanel.setLayout(new java.awt.GridBagLayout());
        pipelineResultTabbedPane.addTab("Summary", summaryParentPanel);

        identificationsParentPanel.setRequestFocusEnabled(false);
        identificationsParentPanel.setLayout(new java.awt.GridBagLayout());
        pipelineResultTabbedPane.addTab("Identifications", identificationsParentPanel);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.PAGE_END;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.7;
        pipelinePanel.add(pipelineResultTabbedPane, gridBagConstraints);

        getContentPane().add(pipelinePanel, "pipelinePanel");

        modificationsParentPanel.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        modificationsParentPanel.setLayout(new java.awt.GridBagLayout());
        getContentPane().add(modificationsParentPanel, "modificationsParentPanel");

        pipelineParamsParentPanel.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        pipelineParamsParentPanel.setLayout(new java.awt.GridBagLayout());
        getContentPane().add(pipelineParamsParentPanel, "pipelineParamsParentPanel");

        viewMenu.setText("View");

        pipelineViewMenuItem.setText("pipeline");
        viewMenu.add(pipelineViewMenuItem);

        modificationsViewMenuItem.setText("modifications");
        viewMenu.add(modificationsViewMenuItem);

        pipelineParamsViewMenuItem.setText("pipeline params");
        viewMenu.add(pipelineParamsViewMenuItem);

        menuBar.add(viewMenu);

        helpMenu.setText("Help");
        menuBar.add(helpMenu);

        setJMenuBar(menuBar);

        pack();
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel experimentSelectionParentPanel;
    private javax.swing.JMenu helpMenu;
    private javax.swing.JPanel identificationsParentPanel;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JPanel modificationsParentPanel;
    private javax.swing.JMenuItem modificationsViewMenuItem;
    private javax.swing.JPanel pipelinePanel;
    private javax.swing.JPanel pipelineParamsParentPanel;
    private javax.swing.JMenuItem pipelineParamsViewMenuItem;
    private javax.swing.JTabbedPane pipelineResultTabbedPane;
    private javax.swing.JMenuItem pipelineViewMenuItem;
    private javax.swing.JPanel summaryParentPanel;
    private javax.swing.JMenu viewMenu;
    // End of variables declaration//GEN-END:variables
}