/*
 *

 */
package com.compomics.pride_asa_pipeline;

import com.compomics.pride_asa_pipeline.config.PropertiesConfigurationHolder;
import com.compomics.pride_asa_pipeline.logic.DbSpectrumAnnotator;
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
public class DbCommandLineRunner {

    private static final Logger LOGGER = Logger.getLogger(DbCommandLineRunner.class);
    private DbSpectrumAnnotator dbSpectrumAnnotator;
    private ResultHandler dbResultHandler;

    public DbSpectrumAnnotator getDbSpectrumAnnotator() {
        return dbSpectrumAnnotator;
    }

    public void setDbSpectrumAnnotator(DbSpectrumAnnotator dbSpectrumAnnotator) {
        this.dbSpectrumAnnotator = dbSpectrumAnnotator;
    }

    public ResultHandler getDbResultHandler() {
        return dbResultHandler;
    }

    public void setDbResultHandler(ResultHandler dbResultHandler) {
        this.dbResultHandler = dbResultHandler;
    }

    /**
     * Runs the experiment in command line mode.
     *
     * @param experimentAccession the experiment accession
     */
    public void runPipeline(String experimentAccession) {
        try {
            //init the annotiation
            dbSpectrumAnnotator.initIdentifications(experimentAccession);

            //check if the experiment has "useful" identifications
            if (dbSpectrumAnnotator.getIdentifications().getCompleteIdentifications().isEmpty()) {
                LOGGER.warn("No useful identifications were found for experiment " + experimentAccession + ". This experiment will be skipped.");
                dbSpectrumAnnotator.clearPipeline();
            } else {
                //check if the maximum systematic mass error is exceeded
                if (dbSpectrumAnnotator.getSpectrumAnnotatorResult().getMassRecalibrationResult().exceedsMaximumSystematicMassError()) {
                    LOGGER.warn("One or more systematic mass error exceed the maximum value of " + PropertiesConfigurationHolder.getInstance().getDouble("massrecalibrator.maximum_systematic_mass_error") + ", experiment " + experimentAccession + " will be skipped.");
                    dbSpectrumAnnotator.clearPipeline();
                } else {
                    //continue with the annotiation
                    dbSpectrumAnnotator.annotate();
                    //write result to file
                    dbResultHandler.writeResultToFile(dbSpectrumAnnotator.getSpectrumAnnotatorResult());
                    dbResultHandler.writeUsedModificationsToFile(dbSpectrumAnnotator.getSpectrumAnnotatorResult());
                }
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    /**
     * Runs the experiment in command line mode, for the experiment accessions
     * found in the given file
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
        Set<String> experimentAccessions = new HashSet<>();

        //read the experiment accession from the file
        try (BufferedReader br = new BufferedReader(new FileReader(experimentAccessionsFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                experimentAccessions.add(line);
            }
        } catch (FileNotFoundException e) {
            LOGGER.error(e.getMessage(), e);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }

        return experimentAccessions;
    }
}
