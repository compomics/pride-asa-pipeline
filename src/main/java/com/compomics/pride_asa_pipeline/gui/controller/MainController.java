/*
 *

 */
package com.compomics.pride_asa_pipeline.gui.controller;

import com.compomics.pride_asa_pipeline.gui.view.MainFrame;
import com.compomics.pride_asa_pipeline.logic.PrideSpectrumAnnotator;
import com.compomics.pride_asa_pipeline.model.SpectrumAnnotatorResult;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.TreeSet;
import java.util.logging.Level;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import org.apache.log4j.Logger;
import org.jdesktop.beansbinding.ELProperty;

/**
 *
 * @author Niels Hulstaert
 */
public class MainController implements ActionListener {

    private static final Logger LOGGER = Logger.getLogger(MainController.class);
    private static final String PIPELINE_PANEL_CARD_NAME = "pipelinePanel";
    private static final String MODIFICATIONS_CARD_NAME = "modificationsParentPanel";
    private static final String PIPELINE_PARAMS_CARD_NAME = "pipelineParamsParentPanel";
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
        String cardName = actionEvent.getActionCommand();

        CardLayout cardLayout = (CardLayout) mainFrame.getMainPanel().getLayout();
        cardLayout.show(mainFrame.getMainPanel(), cardName);
    }

    public void init() {
        //set uncaught exception handler
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                LOGGER.error(e.getMessage(), e);
                showUnexpectedErrorDialog(e.getMessage());
                onAnnotationCanceled();
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

        mainFrame.getPrideSelectionParentPanel().add(experimentSelectionController.getPrideSelectionPanel(), gridBagConstraints);
        mainFrame.getFileSelectionParentPanel().add(experimentSelectionController.getFileSelectionPanel(), gridBagConstraints);
        mainFrame.getModificationsParentPanel().add(modificationsController.getModificationsPanel(), gridBagConstraints);
        mainFrame.getIdentificationsParentPanel().add(pipelineResultController.getIdentificationsPanel(), gridBagConstraints);
        mainFrame.getSummaryParentPanel().add(pipelineResultController.getSummaryPanel(), gridBagConstraints);
        mainFrame.getPipelineParamsParentPanel().add(pipelineParamsController.getPipelineParamsPanel(), gridBagConstraints);

        //add action listeners and action commands       
        mainFrame.getPipelineButton().setActionCommand(PIPELINE_PANEL_CARD_NAME);
        mainFrame.getPipelineButton().addActionListener(this);
        mainFrame.getPipelineParamsButton().setActionCommand(PIPELINE_PARAMS_CARD_NAME);
        mainFrame.getPipelineParamsButton().addActionListener(this);
        mainFrame.getModificationsButton().setActionCommand(MODIFICATIONS_CARD_NAME);
        mainFrame.getModificationsButton().addActionListener(this);


        //set pipeline panel visible in card layout
        CardLayout cardLayout = (CardLayout) mainFrame.getMainPanel().getLayout();
        cardLayout.show(mainFrame.getMainPanel(), PIPELINE_PANEL_CARD_NAME);

        //fit to screen
        mainFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        //set main frame visible
        mainFrame.setLocationRelativeTo(null);
        mainFrame.setVisible(Boolean.TRUE);
    }

    public void showMessageDialog(String title, String message, int messageType) {
        //add message to JTextArea
        JTextArea textArea = new JTextArea(message);
        //put JTextArea in JScrollPane
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(400, 100));
        textArea.setEditable(Boolean.FALSE);

        JOptionPane.showMessageDialog(mainFrame.getContentPane(), scrollPane, title, messageType);
    }

    public void showUnexpectedErrorDialog(String message) {
        showMessageDialog("Unexpected error", "An expected error occured: "
                + "\n" + message
                + "\n" + "please try to rerun the application.", JOptionPane.ERROR_MESSAGE);
    }

    public void onAnnotationFinished(SpectrumAnnotatorResult spectrumAnnotatorResult) {
        pipelineResultController.update(spectrumAnnotatorResult);
    }

    public void onAnnotationCanceled() {
        LOGGER.info("Annotation canceled.");
        prideSpectrumAnnotator.clearPipeline();
        pipelineResultController.clear();
    }
}