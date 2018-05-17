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
package com.compomics.pride_asa_pipeline.core.playground;

import com.compomics.pride_asa_pipeline.core.CommandLineRunner;
import com.compomics.pride_asa_pipeline.core.gui.PipelineProgressMonitor;
import com.compomics.pride_asa_pipeline.core.model.SpectrumAnnotatorResult;
import com.compomics.pride_asa_pipeline.core.service.ModificationService;
import com.compomics.pride_asa_pipeline.core.spring.ApplicationContextProvider;
import com.compomics.pride_asa_pipeline.model.Modification;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.context.ApplicationContext;

/**
 * @author Niels Hulstaert
 */
public class Playground {

    public static void main(String[] args) throws Exception {
        //load application context
        ApplicationContextProvider.getInstance().setDefaultApplicationContext();
        ApplicationContext applicationContext = ApplicationContextProvider.getInstance().getApplicationContext();

        CommandLineRunner commandLineRunner = applicationContext.getBean("commandLineRunner", CommandLineRunner.class);
        commandLineRunner.runPipeline(new File("W:\\PRIDE-DATA\\PRIDE-FTP-DOWNLOAD\\PRIDE_Exp_Complete_Ac_3659.xml"));

        SpectrumAnnotatorResult spectrumAnnotatorResult = commandLineRunner.getSpectrumAnnotator().getSpectrumAnnotatorResult();
        ModificationService modificationService = commandLineRunner.getSpectrumAnnotator().getModificationService();
        Map<Modification, Integer> usedModifications = modificationService.getUsedModifications(spectrumAnnotatorResult);
        Map<Modification, Double> estimateModificationRate = modificationService.estimateModificationRate(usedModifications, spectrumAnnotatorResult, 0.05);

//        PrideXmlSpectrumAnnotator prideXmlSpectrumAnnotator = (PrideXmlSpectrumAnnotator) applicationContext.getBean("prideXmlSpectrumAnnotator");
//        PrideXmlExperimentService iPrideService = prideXmlSpectrumAnnotator.getExperimentService();
//        iPrideService.init(new File("C:\\Users\\niels\\Desktop\\ExampleDataSets\\PRIDE_Exp_Complete_Ac_15346.xml"));
        System.out.println("test");
    }

    public static File filterExperimentAccessions(File experimentAccessionsFile, File resultsDirectory) {
        File filteredExperimentAccessionsFile = null;

        BufferedReader bufferedReader = null;
        PrintWriter printWriter = null;
        try {
            filteredExperimentAccessionsFile = new File("C:\\Users\\niels\\Desktop\\filtered_pride_experiment_accessions.txt");

            //1. read file names in result directory and put them in a set
            Set<String> processedExperimentAccessions = new HashSet<String>();
            if (!resultsDirectory.isDirectory()) {
                return null;
            }
            for (File resultFile : resultsDirectory.listFiles()) {
                processedExperimentAccessions.add(resultFile.getName().substring(0, resultFile.getName().indexOf('.')));
            }

            //2. read experiment accessions file and write the non processed experiment accessions to the new file
            bufferedReader = new BufferedReader(new FileReader(experimentAccessionsFile));
            printWriter = new PrintWriter(new BufferedWriter(new FileWriter(filteredExperimentAccessionsFile)));

            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
                if (!processedExperimentAccessions.contains(line)) {
                    printWriter.println(line);
                }
            }

        } catch (FileNotFoundException ex) {
            PipelineProgressMonitor.error(ex);
        } catch (IOException ex) {
            PipelineProgressMonitor.error(ex);
        } finally {
            try {
                bufferedReader.close();
            } catch (IOException ex) {
                PipelineProgressMonitor.error(ex);
            }
        }

        return filteredExperimentAccessionsFile;
    }
}
