/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pride_asa_pipeline.gui.appender;

import com.compomics.pride_asa_pipeline.gui.controller.PipelineProgressController;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import org.apache.log4j.WriterAppender;
import org.apache.log4j.spi.LoggingEvent;

/**
 *
 * @author Niels Hulstaert
 */
public class PipelineProgressAppender extends WriterAppender {

    private static PipelineProgressController pipelineProgressController;
    private static ExecutorService executorService = Executors.newSingleThreadExecutor();

    public static void setPipelineProgressController(PipelineProgressController pipelineProgressController) {
        PipelineProgressAppender.pipelineProgressController = pipelineProgressController;
    }

    @Override
    public void append(LoggingEvent event) {
        final String message = this.layout.format(event);

        //check if the controller is initialized
        if(pipelineProgressController != null) {
            executorService.submit(new Runnable() {

                @Override
                public void run() {
                    pipelineProgressController.setProgressInfoText(message);
                }
            });
        }

    }
}
