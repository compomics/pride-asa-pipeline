/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pride_asa_pipeline.gui.controller;

import com.compomics.pride_asa_pipeline.gui.view.MainFrame;
import com.compomics.pride_asa_pipeline.pipeline.PrideSpectrumAnnotator;
import java.awt.CardLayout;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JOptionPane;

/**
 *
 * @author niels
 */
public class MainController implements ActionListener {

    private static final String PIPELINE_PANEL_CARD_NAME = "pipelinePanel";
    private static final String MODIFICATIONS_SETTINGS_CARD_NAME = "modificationsParentPanel";
    private static final String PIPELINE_SETTINGS_CARD_NAME = "pipelineParamsParentPanel";
    //view
    private MainFrame mainFrame;
    //child controllers
    private ExperimentProcessController experimentProcessController;
    //services
    private PrideSpectrumAnnotator prideSpectrumAnnotator;

    public MainController() {
    }

    public ExperimentProcessController getExperimentProcessController() {
        return experimentProcessController;
    }

    public void setExperimentProcessController(ExperimentProcessController experimentProcessController) {
        this.experimentProcessController = experimentProcessController;
    }

    public PrideSpectrumAnnotator getPrideSpectrumAnnotator() {
        return prideSpectrumAnnotator;
    }

    public void setPrideSpectrumAnnotator(PrideSpectrumAnnotator prideSpectrumAnnotator) {
        this.prideSpectrumAnnotator = prideSpectrumAnnotator;
    }

    public MainFrame getMainFrame() {
        return mainFrame;
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        String menuItemLabel = actionEvent.getActionCommand();
        String cardName = "";

        if (menuItemLabel.equals(mainFrame.getModificationsViewMenuItem().getText())) {
            cardName = MODIFICATIONS_SETTINGS_CARD_NAME;
        } else if (menuItemLabel.equals(mainFrame.getPipelineParamsViewMenuItem().getText())) {
            cardName = PIPELINE_SETTINGS_CARD_NAME;
        } else if (menuItemLabel.equals(mainFrame.getPipelineViewMenuItem().getText())) {
            cardName = PIPELINE_PANEL_CARD_NAME;
        }

        CardLayout cardLayout = (CardLayout) mainFrame.getContentPane().getLayout();
        cardLayout.show(mainFrame.getContentPane(), cardName);
    }

    public void init() {
        mainFrame = new MainFrame();

        //init child controllers
        experimentProcessController.init();

        //add panel components                        
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;

        mainFrame.getExperimentProcessParentPanel().add(experimentProcessController.getExperimentProcessPanel(), gridBagConstraints);

        //add action listeners        
        mainFrame.getPipelineViewMenuItem().addActionListener(this);
        mainFrame.getModificationsViewMenuItem().addActionListener(this);
        mainFrame.getPipelineParamsViewMenuItem().addActionListener(this);

        //set pipeline panel visible in card layout
        CardLayout cardLayout = (CardLayout) mainFrame.getContentPane().getLayout();
        cardLayout.show(mainFrame.getContentPane(), PIPELINE_PANEL_CARD_NAME);

        //set main frame visible
        mainFrame.setLocationRelativeTo(null);
        mainFrame.setVisible(Boolean.TRUE);
    }

    public void showMessageDialog(String title, String message, int messageType) {
        JOptionPane.showMessageDialog(mainFrame.getContentPane(), message, title, messageType);
    }
}
