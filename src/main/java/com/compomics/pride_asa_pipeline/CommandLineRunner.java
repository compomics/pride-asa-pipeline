/*
 *

 */
package com.compomics.pride_asa_pipeline;

import com.compomics.pride_asa_pipeline.config.PropertiesConfigurationHolder;
import com.compomics.pride_asa_pipeline.logic.PrideSpectrumAnnotator;
import com.compomics.pride_asa_pipeline.service.ResultHandler;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import org.apache.log4j.Logger;

/**
 *
 * @author Niels Hulstaert
 */
public class CommandLineRunner {
    
    private static final Logger LOGGER = Logger.getLogger(CommandLineRunner.class);
    private PrideSpectrumAnnotator prideSpectrumAnnotator;
    private ResultHandler resultHandler;
    
    public PrideSpectrumAnnotator getPrideSpectrumAnnotator() {
        return prideSpectrumAnnotator;
    }
    
    public void setPrideSpectrumAnnotator(PrideSpectrumAnnotator prideSpectrumAnnotator) {
        this.prideSpectrumAnnotator = prideSpectrumAnnotator;
    }
    
    public ResultHandler getResultHandler() {
        return resultHandler;
    }
    
    public void setResultHandler(ResultHandler resultHandler) {
        this.resultHandler = resultHandler;
    }

    /**
     * Runs the experiment in command line mode.
     *
     * @param experimentAccession the experiment accession
     */
    public void runPipeline(String experimentAccession) {
        try {
            //init the annotiation
            prideSpectrumAnnotator.initIdentifications(experimentAccession);

            //check if the experiment has "useful" identifications
            if (prideSpectrumAnnotator.getIdentifications().getCompleteIdentifications().isEmpty()) {
                LOGGER.warn("No useful identifications were found for experiment " + experimentAccession + ". This experiment will be skipped.");
                prideSpectrumAnnotator.clearPipeline();
            } else {
                //check if the maximum systematic mass error is exceeded
                if (prideSpectrumAnnotator.getSpectrumAnnotatorResult().getMassRecalibrationResult().exceedsMaximumSystematicMassError()) {
                    LOGGER.warn("One or more systematic mass error exceed the maximum value of " + PropertiesConfigurationHolder.getInstance().getDouble("massrecalibrator.maximum_systematic_mass_error") + ", experiment " + experimentAccession + " will be skipped.");
                    prideSpectrumAnnotator.clearPipeline();
                } else {
                    //continue with the annotiation
                    prideSpectrumAnnotator.annotate(experimentAccession);
                    //write result to file
                    resultHandler.writeResultToFile(prideSpectrumAnnotator.getSpectrumAnnotatorResult());
                    resultHandler.writeUsedModificationsToFile(prideSpectrumAnnotator.getSpectrumAnnotatorResult());
                }
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    /**
     * Runs the experiment in command line mode, for the experiments found in
     * the
     *
     * @param experimentAccessionsFile the experiment accessions file
     */
    public void runPipeline(File experimentAccessionsFile) {
        Set<String> experimentAccessions = readExperimentAccessions(experimentAccessionsFile);
        
        for (String experimentAccession : experimentAccessions) {
            runPipeline(experimentAccession);
        }
    }
    
    private Set<String> readExperimentAccessions(File experimentAccessionsFile) {
        Set<String> experimentAccessions = new HashSet<String>();

        //read the experiment accession from the file
        try {
            BufferedReader br = new BufferedReader(new FileReader(experimentAccessionsFile));
            
            String line = null;
            while ((line = br.readLine()) != null) {
                experimentAccessions.add(line);
            }
            br.close();
        } catch (FileNotFoundException e) {
            LOGGER.error(e.getMessage(), e);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        
        return experimentAccessions;
    }
}
