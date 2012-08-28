/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pride_asa_pipeline.gui.controller;

import com.compomics.pride_asa_pipeline.gui.appender.PipelineProgressAppender;
import com.compomics.pride_asa_pipeline.gui.view.PipelineProgressDialog;
import com.compomics.pride_asa_pipeline.util.GuiUtils;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 *
 * @author Niels Hulstaert Hulstaert
 */
public class PipelineProgressController extends WindowAdapter {

    private static final int NUMBER_OF_PROGRESS_STEPS = 5;
    private static final String PROGRESS_HEADER_TEXT = "Processing...";
    //model
    private int progress;
    //view
    private PipelineProgressDialog pipelineProgressDialog;
    //parent controller
    private ExperimentSelectionController experimentSelectionController;

    public ExperimentSelectionController getExperimentSelectionController() {
        return experimentSelectionController;
    }

    public void setExperimentSelectionController(ExperimentSelectionController experimentProcessController) {
        this.experimentSelectionController = experimentProcessController;
    }

    public void init() {
        pipelineProgressDialog = new PipelineProgressDialog(experimentSelectionController.getMainController().getMainFrame());
        pipelineProgressDialog.addWindowListener(this);

        pipelineProgressDialog.getProgressBar().setMaximum(NUMBER_OF_PROGRESS_STEPS);

        //set this controller in PipelineProgressAppender
        PipelineProgressAppender.setPipelineProgressController(this);
    }

    public void showProgressBar() {
        pipelineProgressDialog.getProgressHeaderLabel().setText(PROGRESS_HEADER_TEXT);
        progress = 1;
        GuiUtils.centerDialogOnFrame(experimentSelectionController.getMainController().getMainFrame(), pipelineProgressDialog);
        pipelineProgressDialog.setVisible(Boolean.TRUE);
    }

    public void hideProgressDialog() {
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

    @Override
    public void windowClosed(WindowEvent e) {
        super.windowClosed(e);
        experimentSelectionController.onAnnotationCanceled();
    }
       
}
