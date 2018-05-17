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
package com.compomics.pride_asa_pipeline.core;

import com.compomics.software.CompomicsWrapper;
import java.io.File;
import java.net.URISyntaxException;
import com.compomics.pride_asa_pipeline.core.gui.PipelineProgressMonitor;

/**
 *
 * @author Niels Hulstaert
 */
public class StarterWrapper extends CompomicsWrapper {

    /**
     * Starts the launcher by calling the launch method. Use this as the main
     * class in the jar file.
     *
     * @param args
     */
    public StarterWrapper(String[] args) {
        try {
            File jarFile = new File(StarterWrapper.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            String mainClass = "com.compomics.pride_asa_pipeline.PrideAsaPipelineStarter";
            launchTool("Pride ASA Pipeline", jarFile, null, mainClass, args);
        } catch (URISyntaxException ex) {
            PipelineProgressMonitor.error(ex);
        }

    }

    public static void main(String[] args) {
        new StarterWrapper(args);
    }
}
