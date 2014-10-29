/*
 *

 */
package com.compomics.pride_asa_pipeline.core.service;

import com.compomics.pride_asa_pipeline.model.AnalyzerData;
import com.compomics.pride_asa_pipeline.model.Identifications;
import java.io.File;
import java.util.Set;

/**
 * @author Niels Hulstaert
 */
public interface DbExperimentService extends ExperimentService {
  
    /**
     * Loads the experiment identifications
     *
     * @param experimentAccession the experiment accession number
     * @return the experiment identifications
     */
    Identifications loadExperimentIdentifications(String experimentAccession);

    /**
     * Updates the experiment charge states. If it finds that MALDI was used, it
     * will update the list of considered charge states to only contain the
     * charge 1.
     *
     * @param experimentAccession the experiment accession number
     * @param chargeStates the set of charge states
     */
    void updateChargeStates(String experimentAccession, Set<Integer> chargeStates);

    /**
     * Gets the experiment analyzer data
     *
     * @param experimentAccession the experiment accession number
     * @return the experiment analyzer data
     */
    AnalyzerData getAnalyzerData(String experimentAccession);

    /**
     * Gets the number of spectra for a given experiment
     *
     * @param experimentAccession the experiment accession number
     * @return the number of spectra
     */
    long getNumberOfSpectra(String experimentAccession);

    /**
     * Gets protein accessions for a given experiment
     *
     * @param experimentAccession the experiment accession number
     * @return the protein accession set
     */
    Set<String> getProteinAccessions(String experimentAccession);

    /**
     * Gets the number of peptides for a given experiment
     *
     * @param experimentAccession the experiment accession number
     * @return the number of peptides
     */
    long getNumberOfPeptides(String experimentAccession);

    /**
     * Gets the spectra as a file in mgf format
     *
     * @param experimentAccession the experiment accession number
     * @param mgfFile the destination MGF file
     * @param rebuildCache If set to TRUE then the cache will first be rebuilt.
     */
    void getSpectraAsMgfFile(String experimentAccession, File mgfFile, boolean rebuildCache);
}
