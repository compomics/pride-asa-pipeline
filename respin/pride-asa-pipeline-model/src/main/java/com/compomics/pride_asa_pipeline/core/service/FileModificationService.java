/*
 *

 */
package com.compomics.pride_asa_pipeline.core.service;

import com.compomics.pride_asa_pipeline.core.repository.FileParser;
import com.compomics.pride_asa_pipeline.model.Modification;
import java.util.Set;

/**
 *
 * @author Niels Hulstaert
 */
public interface FileModificationService extends ModificationService {

    /**
     * Loads the experiment modifications from the pride XML file
     *
     * @return the list of modifications
     */
    Set<Modification> loadExperimentModifications();

    /**
     * Set the FileParser
     * 
     * @param fileParser 
     */
    void setFileParser(FileParser fileParser);
}
