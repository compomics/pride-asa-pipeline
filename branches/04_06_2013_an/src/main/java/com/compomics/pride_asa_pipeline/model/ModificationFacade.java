package com.compomics.pride_asa_pipeline.model;

/**
 * @author Florian Reisinger
 *         Date: 04-Jun-2010
 * @since $version
 */
public interface ModificationFacade {

    /**
     * Gets the modification mass shift
     * 
     * @return the mass shift value
     */
    public double getMassShift();

    /**
     * Gets the modification name
     * 
     * @return the modification name
     */
    public String getName();


}
