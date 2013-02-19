package com.compomics.pride_asa_pipeline.repository;

import com.compomics.pride_asa_pipeline.model.Identification;
import com.compomics.pride_asa_pipeline.model.Modification;
import java.io.File;
import java.util.List;
import java.util.Map;

/**
 *
 * @author niels
 */
public interface PrideXmlParser {

    /**
     * Inits the parser with the given Pride XML file
     *
     * @param prideXmlFile the Pride XML file
     */
    void init(File prideXmlFile);

    /**
     * Clears the resources used by the parser
     */
    void clear();

    /**
     * Gets the experiment identifications
     *
     * @return the list of experiment identifications
     */
    List<Identification> loadExperimentIdentifications();

    /**
     * Gets the number of spectra
     *
     * @return the number of spectra
     */
    long getNumberOfSpectra();

    /**
     * Gets the number of peptides
     *
     * @return the number of peptides
     */
    long getNumberOfPeptides();

    /**
     * Gets the spectrum metadat for a given experiment; experiment ID,
     * precursor mz value and charge state
     *
     * @return the spectra metadata
     */
    List<Map<String, Object>> getSpectraMetadata();

    /**
     * Gets the spectrum IDs
     *
     * @return the spectrum IDs
     */
    List<String> getSpectrumIds();
    
    /**
     * Gets the modifications found in the file.
     * 
     * @return the found modifications
     */
    List<Modification> getModifications();
}
