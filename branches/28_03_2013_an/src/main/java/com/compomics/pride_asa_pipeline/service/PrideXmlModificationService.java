/*
 *

 */
package com.compomics.pride_asa_pipeline.service;

import com.compomics.pride_asa_pipeline.model.Modification;
import java.util.Set;

/**
 *
 * @author Niels Hulstaert
 */
public interface PrideXmlModificationService extends ModificationService {

    /**
     * Loads the experiment modifications from the pride XML file
     *
     * @return the list of modifications
     */
    Set<Modification> loadExperimentModifications();
}
