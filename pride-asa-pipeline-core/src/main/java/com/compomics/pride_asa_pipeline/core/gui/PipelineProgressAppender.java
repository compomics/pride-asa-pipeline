/*
 *

 */
package com.compomics.pride_asa_pipeline.core.gui;

import com.compomics.pride_asa_pipeline.core.gui.controller.PipelineProgressController;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.log4j.WriterAppender;
import org.apache.log4j.spi.LoggingEvent;

/**
 *
 * @author Niels Hulstaert Hulstaert
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
