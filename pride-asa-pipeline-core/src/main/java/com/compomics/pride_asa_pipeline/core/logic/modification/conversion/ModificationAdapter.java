package com.compomics.pride_asa_pipeline.core.logic.modification.conversion;

/**
 *
 * @author Kenneth Verheggen
 */
public interface ModificationAdapter<T> {

    /**
     * Converts a unimod modification to the desired format
     * @param mod the input modification
     * @return a converted modification
     */
    public T convertModification(UniModModification mod);

}
