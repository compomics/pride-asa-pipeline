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
package com.compomics.pride_asa_pipeline.core.repository;

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
