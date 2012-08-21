/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pride_asa_pipeline.pipeline;

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
 * @author niels
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
        //continue with the annotiation
        prideSpectrumAnnotator.annotate(experimentAccession);
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
            //init the annotiation
            prideSpectrumAnnotator.initAnnotation(experimentAccession);
            //continue with the annotiation
            prideSpectrumAnnotator.annotate(experimentAccession);
        }
    }

    private Set<String> readExperimentAccessions(File experimentAccessionsFile) {
        Set<String> experimentAccessions = new HashSet<String>();

        //read the experiment accession from the file
        try {
            BufferedReader br = new BufferedReader(new FileReader(experimentAccessionsFile));

            String line = null;
            while ((line = br.readLine()) != null) {
                String experimentAccession = line;
                experimentAccessions.add(experimentAccession);
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
