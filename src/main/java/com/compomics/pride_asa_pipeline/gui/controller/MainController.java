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
    private ExperimentSelectionController experimentSelectionController;
    private ModificationsController modificationsController;
    private PipelineResultController pipelineResultController;
    //services
    private PrideSpectrumAnnotator prideSpectrumAnnotator;

    public MainController() {
    }

    public ExperimentSelectionController getExperimentSelectionController() {
        return experimentSelectionController;
    }

    public void setExperimentSelectionController(ExperimentSelectionController experimentSelectionController) {
        this.experimentSelectionController = experimentSelectionController;
    }

    public ModificationsController getModificationsController() {
        return modificationsController;
    }

    public void setModificationsController(ModificationsController modificationsController) {
        this.modificationsController = modificationsController;
    }

    public PrideSpectrumAnnotator getPrideSpectrumAnnotator() {
        return prideSpectrumAnnotator;
    }

    public void setPrideSpectrumAnnotator(PrideSpectrumAnnotator prideSpectrumAnnotator) {
        this.prideSpectrumAnnotator = prideSpectrumAnnotator;
    }

    public PipelineResultController getPipelineResultController() {
        return pipelineResultController;
    }

    public void setPipelineResultController(PipelineResultController pipelineResultController) {
        this.pipelineResultController = pipelineResultController;
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
        experimentSelectionController.init();
        modificationsController.init();
        pipelineResultController.init();

        //add panel components                        
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;

        mainFrame.getExperimentSelectionParentPanel().add(experimentSelectionController.getExperimentSelectionPanel(), gridBagConstraints);
        mainFrame.getModificationsParentPanel().add(modificationsController.getModificationsPanel(), gridBagConstraints);
        mainFrame.getIdentificationsParentPanel().add(pipelineResultController.getPipelineResultPanel(), gridBagConstraints);

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

    public void onPipelineFinished() {
        pipelineResultController.updateIdentifications();
    }
}
