/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pride_asa_pipeline.gui.controller;

import com.compomics.pride_asa_pipeline.gui.appender.PipelineProgressAppender;
import com.compomics.pride_asa_pipeline.gui.view.PipelineProgressDialog;

/**
 *
 * @author Niels Hulstaert
 */
public class PipelineProgressController {

    private static final int NUMBER_OF_PROGRESS_STEPS = 5;
    private static final String PROGRESS_HEADER_TEXT = "Processing...";
    
    private int progress;
    //parent controller
    private ExperimentProcessController experimentProcessController;
    //view
    private PipelineProgressDialog pipelineProgressDialog;

    public PipelineProgressController() {
    }

    public ExperimentProcessController getExperimentProcessController() {
        return experimentProcessController;
    }

    public void setExperimentProcessController(ExperimentProcessController experimentProcessController) {
        this.experimentProcessController = experimentProcessController;
    }

    public void init() {
        pipelineProgressDialog = new PipelineProgressDialog(experimentProcessController.getMainController().getMainFrame());

        pipelineProgressDialog.getProgressBar().setMaximum(NUMBER_OF_PROGRESS_STEPS);

        //set this controller in PipelineProgressAppender
        PipelineProgressAppender.setPipelineProgressController(this);
    }

    public void showProgressBar() {
        pipelineProgressDialog.getProgressHeaderLabel().setText(PROGRESS_HEADER_TEXT);
        progress = 1;
        pipelineProgressDialog.setVisible(Boolean.TRUE);
    }
    
    public void hideProgressBar() {        
        pipelineProgressDialog.setVisible(Boolean.FALSE);
    }

    public void setProgressInfoText(String progressInfoText) {
        pipelineProgressDialog.getProgressInfoLabel().setText(progressInfoText);

        pipelineProgressDialog.getProgressBar().setValue(progress);
        progress++;

        //repaint view
        pipelineProgressDialog.validate();
        pipelineProgressDialog.repaint();
    }
}
