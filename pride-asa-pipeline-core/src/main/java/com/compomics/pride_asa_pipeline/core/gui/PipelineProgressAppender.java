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

    public PipelineProgressAppender(){
        
    }
    
    @Override
    public void append(LoggingEvent event) {
        final String message = this.layout.format(event);
        System.out.println("IT is passing here" + message);
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
