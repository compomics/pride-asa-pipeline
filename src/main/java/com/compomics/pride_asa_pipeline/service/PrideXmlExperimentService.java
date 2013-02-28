/*
 *

 */
package com.compomics.pride_asa_pipeline.service;

import com.compomics.pride_asa_pipeline.model.AnalyzerData;
import com.compomics.pride_asa_pipeline.model.Identifications;
import java.io.File;
import java.util.Set;

/**
 * @author Niels Hulstaert
 */
public interface PrideXmlExperimentService extends ExperimentService {

    /**
     * Loads the experiment identifications
     *
     * @param experimentPrideXmlFile the experiment pride XML file
     * @return the experiment identifications
     */
    Identifications loadExperimentIdentifications(File experimentPrideXmlFile);

    /**
     * Updates the experiment charge states. If it finds that MALDI was used, it
     * will update the list of considered charge states to only contain the
     * charge 1.
     *
     * @param chargeStates the set of charge states
     */
    void updateChargeStates(Set<Integer> chargeStates);

    /**
     * Gets the experiment analyzer data
     *
     * @return the experiment analyzer data
     */
    AnalyzerData getAnalyzerData();

    /**
     * Gets the number of spectra for a given experiment
     *
     * @return the number of spectra
     */
    long getNumberOfSpectra();

    /**
     * Gets protein accessions for a given experiment
     *
     * @return the protein accession set
     */
    Set<String> getProteinAccessions();

    /**
     * Gets the number of peptides for a given experiment
     *
     * @return the number of peptides
     */
    long getNumberOfPeptides();

    /**
     * Gets the spectra as a file in mgf format
     *
     * @return the spectra file
     */
    File getSpectraAsMgfFile();
}
