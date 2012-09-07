/*
 *

 */
package com.compomics.pride_asa_pipeline;

import com.compomics.pride_asa_pipeline.config.PropertiesConfigurationHolder;
import com.compomics.pride_asa_pipeline.logic.PrideSpectrumAnnotator;
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

    public PrideSpectrumAnnotator getPrideSpectrumAnnotator() {
        return prideSpectrumAnnotator;
    }

    public void setPrideSpectrumAnnotator(PrideSpectrumAnnotator prideSpectrumAnnotator) {
        this.prideSpectrumAnnotator = prideSpectrumAnnotator;
    }

    /**
     * Runs the experiment in command line mode.
     *
     * @param experimentAccession the experiment accession
     */
    public void runPipeline(String experimentAccession) {
        //init the annotiation
        prideSpectrumAnnotator.initAnnotation(experimentAccession);

        //check if the experiment has "useful" identifications
        if (prideSpectrumAnnotator.getIdentifications().getCompleteIdentifications().isEmpty()) {
            LOGGER.warn("One or more systematic mass error exceed the maximum value of " + PropertiesConfigurationHolder.getInstance().getDouble("massrecalibrator.maximum_systematic_mass_error") + ", experiment " + experimentAccession + " will be skipped.");
            prideSpectrumAnnotator.clearPipeline();
        }
        //check if the maximum systematic mass error is exceeded
        if (prideSpectrumAnnotator.getSpectrumAnnotatorResult().getMassRecalibrationResult().exceedsMaximumSystematicMassError()) {
            //continue with the annotiation
            prideSpectrumAnnotator.annotate(experimentAccession);
        } else {
            LOGGER.warn("One or more systematic mass error exceed the maximum value of " + PropertiesConfigurationHolder.getInstance().getDouble("massrecalibrator.maximum_systematic_mass_error") + ", experiment " + experimentAccession + " will be skipped.");
            prideSpectrumAnnotator.clearPipeline();
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
