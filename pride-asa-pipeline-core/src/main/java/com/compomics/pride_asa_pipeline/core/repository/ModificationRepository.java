/*
 *

 */
package com.compomics.pride_asa_pipeline.core.repository;

import com.compomics.pride_asa_pipeline.model.Modification;
import java.util.List;

/**
 *
 * @author Niels Hulstaert
 */
/**
 *
 * @author Kenneth Verheggen
 */
public interface ModificationRepository {

    /**
     * Gets the peptide modifications
     *
     * @param peptideId the pride peptide ID
     * @return the peptide modifications
     */
    List<Modification> getModificationsByPeptideId(long peptideId);

    /**
     * Gets the peptide modifications
     *
     * @param experimentId the pride experiment ID
     * @return the peptide modifications that are annotated in the Pride public
     * database instance
     */
    List<Modification> getModificationsByExperimentId(String experimentId);

}
