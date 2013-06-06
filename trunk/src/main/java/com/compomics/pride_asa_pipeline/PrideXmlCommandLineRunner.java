/*
 *

 */
package com.compomics.pride_asa_pipeline;

import com.compomics.pride_asa_pipeline.config.PropertiesConfigurationHolder;
import com.compomics.pride_asa_pipeline.logic.PrideSpectrumAnnotator;
import com.compomics.pride_asa_pipeline.logic.PrideXmlSpectrumAnnotator;
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
public class PrideXmlCommandLineRunner {

    private static final Logger LOGGER = Logger.getLogger(PrideXmlCommandLineRunner.class);
    private PrideXmlSpectrumAnnotator prideXmlSpectrumAnnotator;
    private ResultHandler prideXmlResultHandler;

    public PrideXmlSpectrumAnnotator getPrideXmlSpectrumAnnotator() {
        return prideXmlSpectrumAnnotator;
    }

    public void setPrideXmlSpectrumAnnotator(PrideXmlSpectrumAnnotator prideXmlSpectrumAnnotator) {
        this.prideXmlSpectrumAnnotator = prideXmlSpectrumAnnotator;
    }

    public ResultHandler getPrideXmlResultHandler() {
        return prideXmlResultHandler;
    }

    public void setPrideXmlResultHandler(ResultHandler prideXmlResultHandler) {
        this.prideXmlResultHandler = prideXmlResultHandler;
    }

    /**
     * Runs the pride XML file in command line mode.
     *
     * @param prideXmlFile the pride XML file
     * @param singlePrideXml is the file a single PRIDE XML or is does it contain PRIDE XML file paths
     */
    public void runPrideXmlPipeline(File prideXmlFile, boolean singlePrideXml) {
        try {
            if(singlePrideXml){
                runPrideXmlPipeline(prideXmlFile);
            }
            else{
                Set<String> prideXmlPaths = readPrideXmlPaths(prideXmlFile);
                for(String prideXmlFilePath : prideXmlPaths){
                    File prideXml = new File(prideXmlFilePath);
                    if(prideXml.exists()){
                        runPrideXmlPipeline(prideXml);
                    }                  
                    else{
                        LOGGER.error("The given PRIDE XML file " + prideXml + " could not be found. This file will be skipped.");
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
    
    /**
     * Runs the pride XML file in command line mode.
     *
     * @param prideXmlFile the pride XML file
     */
    private void runPrideXmlPipeline(File prideXmlFile) {
        try {
            String experimentAccession = prideXmlFile.getName().substring(0, prideXmlFile.getName().indexOf(".xml"));

            //init the annotiation
            prideXmlSpectrumAnnotator.initIdentifications(prideXmlFile);

            //check if the experiment has "useful" identifications
            if (prideXmlSpectrumAnnotator.getIdentifications().getCompleteIdentifications().isEmpty()) {
                LOGGER.warn("No useful identifications were found for experiment " + experimentAccession + ". This experiment will be skipped.");
                prideXmlSpectrumAnnotator.clearTmpResources();
            } else {
                //check if the maximum systematic mass error is exceeded
                if (prideXmlSpectrumAnnotator.getSpectrumAnnotatorResult().getMassRecalibrationResult().exceedsMaximumSystematicMassError()) {
                    LOGGER.warn("One or more systematic mass error exceed the maximum value of " + PropertiesConfigurationHolder.getInstance().getDouble("massrecalibrator.maximum_systematic_mass_error") + ", experiment " + experimentAccession + " will be skipped.");
                    prideXmlSpectrumAnnotator.clearTmpResources();
                } else {
                    //continue with the annotiation
                    prideXmlSpectrumAnnotator.annotate(prideXmlFile);
                    //write result to file
                    prideXmlResultHandler.writeResultToFile(prideXmlSpectrumAnnotator.getSpectrumAnnotatorResult());
                    prideXmlResultHandler.writeUsedModificationsToFile(prideXmlSpectrumAnnotator.getSpectrumAnnotatorResult());
                }
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
    
    private Set<String> readPrideXmlPaths(File experimentAccessionsFile) {
        Set<String> prideXmlPaths = new HashSet<String>();

        //read the experiment accession from the file
        try {
            BufferedReader br = new BufferedReader(new FileReader(experimentAccessionsFile));
            
            String line = null;
            while ((line = br.readLine()) != null) {
                prideXmlPaths.add(line);
            }
            br.close();
        } catch (FileNotFoundException e) {
            LOGGER.error(e.getMessage(), e);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        
        return prideXmlPaths;
    }
}
