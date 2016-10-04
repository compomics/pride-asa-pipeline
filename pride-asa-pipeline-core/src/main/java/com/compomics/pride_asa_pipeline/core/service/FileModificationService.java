/*
 *

 */
package com.compomics.pride_asa_pipeline.core.service;

import com.compomics.pride_asa_pipeline.core.repository.FileParser;
import com.compomics.pride_asa_pipeline.model.Modification;
import java.util.Set;
import uk.ac.ebi.pride.utilities.data.controller.impl.ControllerImpl.CachedDataAccessController;

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
     * Sets the active assay
     *
     */
    void setActiveAssay(String assayIdentifier);
}