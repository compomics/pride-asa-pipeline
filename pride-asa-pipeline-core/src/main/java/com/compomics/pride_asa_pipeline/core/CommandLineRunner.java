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

import com.compomics.pride_asa_pipeline.core.cache.ParserCache;
import com.compomics.pride_asa_pipeline.core.config.PropertiesConfigurationHolder;
import com.compomics.pride_asa_pipeline.core.gui.PipelineProgressMonitor;
import com.compomics.pride_asa_pipeline.core.logic.spectrum.annotation.AbstractSpectrumAnnotator;
import com.compomics.pride_asa_pipeline.core.repository.impl.combo.WebServiceFileExperimentRepository;
import com.compomics.pride_asa_pipeline.core.repository.impl.file.FileExperimentRepository;
import com.compomics.pride_asa_pipeline.core.service.ResultHandler;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Kenneth Verheggen
 */
public class CommandLineRunner {

    private AbstractSpectrumAnnotator spectrumAnnotator;
    private ResultHandler resultHandler;

    public AbstractSpectrumAnnotator getSpectrumAnnotator() {
        return spectrumAnnotator;
    }

    public void setSpectrumAnnotator(AbstractSpectrumAnnotator spectrumAnnotator) {
        this.spectrumAnnotator = spectrumAnnotator;
    }

    public ResultHandler getresultHandler() {
        return resultHandler;
    }

    public void setresultHandler(ResultHandler resultHandler) {
        this.resultHandler = resultHandler;
    }

    public void runPipeline(String experimentAccession) throws Exception {
        //set up the repository to cache this experiment
        PipelineProgressMonitor.info("Setting up experiment repository for assay " + experimentAccession);
        WebServiceFileExperimentRepository experimentRepository = new WebServiceFileExperimentRepository();
        experimentRepository.addAssay(experimentAccession);
        //the cache should only have one for now?
        String entry = ParserCache.getInstance().getLoadedFiles().keySet().iterator().next();
        PipelineProgressMonitor.info(entry + " was found in the parser cache");
        execute(experimentAccession);
    }

        public void runPipeline(File identifciationsFile,File...peakFiles) throws Exception {
        //set up the repository to cache this experiment
        PipelineProgressMonitor.info("Setting up experiment repository for assay " + identifciationsFile.getName());
        FileExperimentRepository experimentRepository = new FileExperimentRepository();
        if(identifciationsFile.getName().toLowerCase().endsWith(".xml") && peakFiles.length==0){
                   experimentRepository.addPrideXMLFile(identifciationsFile.getName(),identifciationsFile);
        }else {
                   experimentRepository.addMzID(identifciationsFile.getName(),identifciationsFile,Arrays.asList(peakFiles));            
        }
 
        //the cache should only have one for now?
        String entry = ParserCache.getInstance().getLoadedFiles().keySet().iterator().next();
        PipelineProgressMonitor.info(entry + " was found in the parser cache");
        execute(identifciationsFile.getName());
    }
    
    /**
     * Runs the experiment in command line mode.
     *
     * @param experimentAccession the experiment accession
     */
    private void execute(String experimentAccession) {
        try {

            //init the annotiation
            spectrumAnnotator.initIdentifications(experimentAccession);

            //check if the experiment has "useful" identifications
            if (spectrumAnnotator.getIdentifications().getCompleteIdentifications().isEmpty()) {
                PipelineProgressMonitor.warn("No useful identifications were found for experiment " + experimentAccession + ". This experiment will be skipped.");
                spectrumAnnotator.clearPipeline();
            } else {
                //check if the maximum systematic mass error is exceeded
                if (spectrumAnnotator.getSpectrumAnnotatorResult().getMassRecalibrationResult().exceedsMaximumSystematicMassError()) {
                    PipelineProgressMonitor.warn("One or more systematic mass error exceed the maximum value of " + PropertiesConfigurationHolder.getInstance().getDouble("massrecalibrator.maximum_systematic_mass_error") + ", experiment " + experimentAccession + " will be skipped.");
                    spectrumAnnotator.clearPipeline();
                } else {
                    //continue with the annotiation
                    spectrumAnnotator.annotate(experimentAccession);
                    //write result to file
                    resultHandler.writeResultToFile(spectrumAnnotator.getSpectrumAnnotatorResult());
                    resultHandler.writeUsedModificationsToFile(experimentAccession, spectrumAnnotator.getModificationService().getUsedModifications(spectrumAnnotator.getSpectrumAnnotatorResult()).keySet());
                }
            }
        } catch (Exception e) {
            PipelineProgressMonitor.error(e.getMessage(), e);
        }
    }

    /**
     * Runs the experiment in command line mode, for the experiment accessions
     * found in the given file
     *
     * @param experimentAccessionsFile the experiment accessions file
     */
    public void runPipeline(File experimentAccessionsFile) throws Exception {
        Set<String> experimentAccessions = readExperimentAccessions(experimentAccessionsFile);

        for (String experimentAccession : experimentAccessions) {
            runPipeline(experimentAccession);
        }
    }

    private Set<String> readExperimentAccessions(File experimentAccessionsFile) {
        Set<String> experimentAccessions = new HashSet<>();

        //read the experiment accession from the file
        try (BufferedReader br = new BufferedReader(new FileReader(experimentAccessionsFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                experimentAccessions.add(line);
            }
        } catch (FileNotFoundException e) {
            PipelineProgressMonitor.error(e.getMessage(), e);
        } catch (IOException e) {
            PipelineProgressMonitor.error(e.getMessage(), e);
        }

        return experimentAccessions;
    }
}
