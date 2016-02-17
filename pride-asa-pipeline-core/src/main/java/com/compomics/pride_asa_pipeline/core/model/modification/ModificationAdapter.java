package com.compomics.pride_asa_pipeline.core.model.modification;

import com.compomics.pride_asa_pipeline.core.exceptions.ParameterExtractionException;

/**
 *
 * @author Kenneth Verheggen
 */
public interface ModificationAdapter<T> {

    /**
     * Converts a unimod modification to the desired format
     * @param mod the input modification
     * @return a converted modification
     * @throws ParameterExtractionException if the extraction is suspicious (large different between ptm mass and reported mass for example)
     */
    public T convertModification(PRIDEModification mod) throws ParameterExtractionException;

}
