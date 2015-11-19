/*
 *

 */
package com.compomics.pride_asa_pipeline.core.service;

import com.compomics.pride_asa_pipeline.model.Modification;
import com.compomics.pride_asa_pipeline.model.Peptide;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Niels Hulstaert
 */
public interface DbModificationService extends ModificationService {    
    
    /**
     * Loads the experiment modifications from pride
     *
     * @param completePeptides the experiment complete peptides
     * @return the list of modifications
     */
    Set<Modification> loadExperimentModifications(List<Peptide> completePeptides);

    /**
     * Loads the experiment modifications from a pride experiment
     *
     * @param experimentId the experiment identifier
     * @return the list of modifications
     */
    Set<Modification> loadExperimentModifications(String experimentId);   
}
