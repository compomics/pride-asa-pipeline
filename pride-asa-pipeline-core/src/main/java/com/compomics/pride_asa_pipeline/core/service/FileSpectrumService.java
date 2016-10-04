/*
 *

 */
package com.compomics.pride_asa_pipeline.core.service;


/**
 *
 * @author Niels Hulstaert
 */
public interface FileSpectrumService extends SpectrumService {

    /**
     * Sets the active assay
     *
     */
    void setActiveAssay(String assayIdentifier);

}