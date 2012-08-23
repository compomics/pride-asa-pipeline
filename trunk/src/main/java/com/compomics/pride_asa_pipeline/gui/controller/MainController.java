/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pride_asa_pipeline.gui.controller;

import com.compomics.pride_asa_pipeline.gui.view.MainFrame;
import com.compomics.pride_asa_pipeline.logic.PrideSpectrumAnnotator;
import com.compomics.pride_asa_pipeline.service.ExperimentService;
import java.awt.CardLayout;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Level;
import javax.swing.JOptionPane;
import org.apache.log4j.Logger;
import org.jdesktop.beansbinding.ELProperty;

/**
 *
 * @author niels
 */
public class MainController implements ActionListener {
    
    private static final Logger LOGGER = Logger.getLogger(MainController.class);
    private static final String PIPELINE_PANEL_CARD_NAME = "pipelinePanel";
    private static final String MODIFICATIONS_SETTINGS_CARD_NAME = "modificationsParentPanel";
    private static final String PIPELINE_SETTINGS_CARD_NAME = "pipelineParamsParentPanel";
    //view
    private MainFrame mainFrame;
    //child controllers
    private ExperimentSelectionController experimentSelectionController;
    private ModificationsController modificationsController;
    private PipelineResultController pipelineResultController;
    private PipelineParamsController pipelineParamsController;
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
    
    public PipelineParamsController getPipelineParamsController() {
        return pipelineParamsController;
    }
    
    public void setPipelineParamsController(PipelineParamsController pipelineParamsController) {
        this.pipelineParamsController = pipelineParamsController;
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
        //set uncaught exception handler
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                showUnexpectedErrorDialog(e.getMessage());
            }
        });
        
        mainFrame = new MainFrame();

        //workaround for betterbeansbinding logging issue
        org.jdesktop.beansbinding.util.logging.Logger.getLogger(ELProperty.class.getName()).setLevel(Level.SEVERE);

        //init child controllers
        experimentSelectionController.init();
        modificationsController.init();
        pipelineResultController.init();
        pipelineParamsController.init();

        //add panel components                        
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        
        mainFrame.getExperimentSelectionParentPanel().add(experimentSelectionController.getExperimentSelectionPanel(), gridBagConstraints);
        mainFrame.getModificationsParentPanel().add(modificationsController.getModificationsPanel(), gridBagConstraints);
        mainFrame.getIdentificationsParentPanel().add(pipelineResultController.getIdentificationsPanel(), gridBagConstraints);
        mainFrame.getSummaryParentPanel().add(pipelineResultController.getSummaryPanel(), gridBagConstraints);
        mainFrame.getPipelineParamsParentPanel().add(pipelineParamsController.getPipelineParamsPanel(), gridBagConstraints);

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
    
    public void showUnexpectedErrorDialog(String message) {
        showMessageDialog("Unexpected error", "Un expected error occured: "
                + "\n" + message
                + "\n" + ", please try to rerun the application.", JOptionPane.ERROR_MESSAGE);
    }
    
    public void onAnnotationFinished() {
        pipelineResultController.updateIdentifications();
        pipelineResultController.updateSummary();
    }
    
    public void onAnnotationCanceled() {
        LOGGER.info("Annotation canceled.");
        prideSpectrumAnnotator.clearPipeline();
        pipelineResultController.clear();
    }
}
