/*
 *

 */
package com.compomics.pride_asa_pipeline.core.repository;

import com.compomics.pride_asa_pipeline.model.AnalyzerData;
import com.compomics.pride_asa_pipeline.model.Identification;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Niels Hulstaert
 */
public interface ExperimentRepository {

    /**
     * Finds all experiment accessions
     *
     * @return the map of experiment accessions (key: experiment accession,
     * value: experiment title)
     */
    Map<String, String> findAllExperimentAccessions();

    /**
     * Finds all experiment accessions by taxonomy ID
     *
     * @param taxonomyId the species taxonomy ID
     * @return the map of experiment accessions (key: experiment accession,
     * value: experiment title)
     */
    Map<String, String> findExperimentAccessionsByTaxonomy(int taxonomyId);

    /**
     * Gets the experiment identifications
     *
     * @param experimentAccession the experiment accession
     * @return the list of experiment identifications
     */
    List<Identification> loadExperimentIdentifications(String experimentAccession);

    /**
     * Gets the analyzer sources as a map (key: cv accession, value: cv value)
     *
     * @param experimentAccession the experiment accession number
     * @return the analyzer source map
     */
    Map<String, String> getAnalyzerSources(String experimentAccession);

    /**
     * Gets the experiment analyzer data
     *
     * @param experimentAccession the experiment accession number
     * @return the experiment analyzer data list
     */
    List<AnalyzerData> getAnalyzerData(String experimentAccession);

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
     * @return the protein accession list
     */
    List<String> getProteinAccessions(String experimentAccession);

    /**
     * Gets the number of peptides for a given experiment
     *
     * @param experimentAccession the experiment accession number
     * @return the number of peptides
     */
    long getNumberOfPeptides(String experimentAccession);

    /**
     * Gets the spectrum metadat for a given experiment; experiment ID,
     * precursor mz value and charge state
     *
     * @param experimentAccession the experiment accession
     * @return the spectra metadata
     */
    List<Map<String, Object>> getSpectraMetadata(String experimentAccession);
}
