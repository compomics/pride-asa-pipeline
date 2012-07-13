/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pride_asa_pipeline.gui.controller;

import com.compomics.pride_asa_pipeline.gui.view.ExperimentSelectionPanel;
import com.compomics.pride_asa_pipeline.service.ExperimentService;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Map;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import org.apache.log4j.Logger;

/**
 *
 * @author Niels Hulstaert
 */
public class ExperimentSelectionController {

    private static final Logger LOGGER = Logger.getLogger(ExperimentSelectionController.class);
    private static final String EXPERIMENT_ACCESSION_SEPARATOR = ":";    
    //model
    private Integer taxonomyId;
    //view
    private ExperimentSelectionPanel experimentSelectionPanel;
    //parent controller
    private MainController mainController;
    //child controllers
    private PipelineProgressController pipelineProgressController;
    //services
    private ExperimentService experimentService;

    public ExperimentSelectionController() {
    }

    public ExperimentService getExperimentService() {
        return experimentService;
    }

    public void setExperimentService(ExperimentService experimentService) {
        this.experimentService = experimentService;
    }

    public MainController getMainController() {
        return mainController;
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public ExperimentSelectionPanel getExperimentSelectionPanel() {
        return experimentSelectionPanel;
    }

    public PipelineProgressController getPipelineProgressController() {
        return pipelineProgressController;
    }

    public void setPipelineProgressController(PipelineProgressController pipelineProgressController) {
        this.pipelineProgressController = pipelineProgressController;
    }

    public void init() {
        experimentSelectionPanel = new ExperimentSelectionPanel();

        //init child controllers
        pipelineProgressController.init();

        //fill combo box
        updateComboBox(experimentService.findAllExperimentAccessions());

        //disable taxonomy text field
        experimentSelectionPanel.getTaxonomyTextField().setEnabled(Boolean.FALSE);

        //add action listeners
        experimentSelectionPanel.getTaxonomyFilterCheckBox().addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                if (experimentSelectionPanel.getTaxonomyFilterCheckBox().isSelected()) {
                    //enable taxonomy text field
                    experimentSelectionPanel.getTaxonomyTextField().setEnabled(Boolean.TRUE);
                    filterExperimentAccessions();
                } else {
                    //disable taxonomy text field
                    experimentSelectionPanel.getTaxonomyTextField().setEnabled(Boolean.FALSE);
                    //reset combo box                    
                    updateComboBox(experimentService.findAllExperimentAccessions());
                    taxonomyId = null;
                }
            }
        });

        experimentSelectionPanel.getTaxonomyTextField().addFocusListener(new FocusListener() {

            @Override
            public void focusGained(FocusEvent fe) {
            }

            @Override
            public void focusLost(FocusEvent fe) {
                filterExperimentAccessions();
            }
        });

        experimentSelectionPanel.getExperimentProcessButton().addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                //execute pipeline worker
                PipelineWorker pipelineWorker = new PipelineWorker();
                pipelineWorker.execute();

                //disable process button
                experimentSelectionPanel.getExperimentProcessButton().setEnabled(Boolean.FALSE);
            }
        });
    }

    private void filterExperimentAccessions() {
        if (!experimentSelectionPanel.getTaxonomyTextField().getText().isEmpty()) {
            try {
                Integer newTaxonomyId = Integer.parseInt(experimentSelectionPanel.getTaxonomyTextField().getText());
                if (taxonomyId != newTaxonomyId) {
                    taxonomyId = newTaxonomyId;
                    updateComboBox(experimentService.findExperimentAccessionsByTaxonomy(taxonomyId));
                }
            } catch (NumberFormatException e) {
                mainController.showMessageDialog("Format error", "Please insert a correct taxonomy ID (e.g. Homo Sapiens ID: 9606)", JOptionPane.ERROR_MESSAGE);
                experimentSelectionPanel.getTaxonomyTextField().setText("");
            }
        }
    }

    private void updateComboBox(Map<String, String> experimentAccessions) {
        //empty combo box
        experimentSelectionPanel.getExperimentSelectionComboBox().removeAllItems();
        //load experiment accessions and fill combo box        
        for (String experimentAccession : experimentAccessions.keySet()) {
            experimentSelectionPanel.getExperimentSelectionComboBox().addItem(experimentAccession + EXPERIMENT_ACCESSION_SEPARATOR + " " + experimentAccessions.get(experimentAccession));
        }
    }

    private String getExperimentAccesion() {
        String experimentAccession = null;
        if (experimentSelectionPanel.getExperimentSelectionComboBox().getSelectedItem() != null) {
            String comboBoxString = experimentSelectionPanel.getExperimentSelectionComboBox().getSelectedItem().toString();
            experimentAccession = comboBoxString.substring(0, comboBoxString.indexOf(EXPERIMENT_ACCESSION_SEPARATOR));
        }

        return experimentAccession;
    }

    //swing worker
    private class PipelineWorker extends SwingWorker<Void, Void> {

        @Override
        protected Void doInBackground() throws Exception {
            //show progress bar
            pipelineProgressController.showProgressBar();

            mainController.getPrideSpectrumAnnotator().annotate(getExperimentAccesion());

            return null;
        }

        @Override
        protected void done() {
            //enable process button
            experimentSelectionPanel.getExperimentProcessButton().setEnabled(Boolean.FALSE);
            
            mainController.onPipelineFinished();
            
            //hide progress bar
            pipelineProgressController.hideProgressBar();
        }
    }
}
