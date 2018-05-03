/* 
 * Copyright 2018 compomics.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.compomics.pride_asa_pipeline.core.gui.controller;

import com.compomics.pride_asa_pipeline.core.gui.PipelineProgressAppender;
import com.compomics.pride_asa_pipeline.core.util.GuiUtils;
import com.compomics.util.gui.waiting.waitinghandlers.ProgressDialogX;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 *
 * @author Niels Hulstaert Hulstaert
 * @author Harald Barsnes
 */
public class PipelineProgressController extends WindowAdapter {

    //model
    private int progress;
    private boolean progressFinished;
    //view
    private ProgressDialogX pipelineProgressDialog;
    //parent controller
    private ExperimentSelectionController experimentSelectionController;        

    public ExperimentSelectionController getExperimentSelectionController() {
        return experimentSelectionController;
    }

    public void setExperimentSelectionController(ExperimentSelectionController experimentProcessController) {
        this.experimentSelectionController = experimentProcessController;
    }

    public void init() {
        progressFinished = Boolean.FALSE;
        //set this controller in PipelineProgressAppender
        PipelineProgressAppender.setPipelineProgressController(this);
    }

    public void showProgressBar(int numberOfProgressSteps, String progressHeaderText) {

        pipelineProgressDialog = new ProgressDialogX(experimentSelectionController.getMainController().getMainFrame(),
                Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/pride-asap.png")),
                Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/pride-asap-orange.png")),
                true);
        pipelineProgressDialog.addWindowListener(this);

        pipelineProgressDialog.getProgressBar().setMaximum(numberOfProgressSteps + 1);
        pipelineProgressDialog.setTitle(progressHeaderText + " Please Wait...");
        progress = 1;
        GuiUtils.centerDialogOnFrame(experimentSelectionController.getMainController().getMainFrame(), pipelineProgressDialog);
        progressFinished = Boolean.FALSE;

        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    pipelineProgressDialog.setVisible(true);
                } catch (IndexOutOfBoundsException e) {
                    // ignore
                }
            }
        }, "ProgressDialog").start();
    }
    
    public boolean isRunCancelled () {
        return pipelineProgressDialog.isRunCanceled();
    }

    public void hideProgressDialog() {
        progressFinished = true;
        pipelineProgressDialog.setRunFinished();
        pipelineProgressDialog.setVisible(Boolean.FALSE);
    }

    public void setProgressInfoText(String progressInfoText) {
        pipelineProgressDialog.setString(progressInfoText);

        pipelineProgressDialog.getProgressBar().setValue(progress);
        progress++;

        //repaint view
        pipelineProgressDialog.validate();
        pipelineProgressDialog.repaint();
    }

    @Override
    public void windowClosed(WindowEvent e) {
        super.windowClosed(e);
        if (!progressFinished) {
            experimentSelectionController.onAnnotationCanceled();
        } 
    }

    @Override
    public void windowClosing(WindowEvent e) {
        super.windowClosing(e);
        e.getWindow().dispose();
        if (!progressFinished) {
            experimentSelectionController.onAnnotationCanceled();
        }
    }
}
