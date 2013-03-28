/*
 *

 */
package com.compomics.pride_asa_pipeline.service;

import com.compomics.pride_asa_pipeline.model.AnalyzerData;
import com.compomics.pride_asa_pipeline.model.Identifications;
import java.io.File;
import java.util.Map;
import java.util.Set;

/**
 * @author Niels Hulstaert
 */
public interface ExperimentService {

    /**
     * Finds all experiment accessions
     *
     * @return the map of experiment accessions (key: experiment accession,
     * value: experiment title)
     */
    Map<String, String> findAllExperimentAccessions();

    /**
     * Finds experiment accessions by taxonomy ID
     *
     * @param taxonomyId the species taxonomy ID
     * @return the map of experiment accessions (key: experiment accession,
     * value: experiment title)
     */
    Map<String, String> findExperimentAccessionsByTaxonomy(int taxonomyId);
}
