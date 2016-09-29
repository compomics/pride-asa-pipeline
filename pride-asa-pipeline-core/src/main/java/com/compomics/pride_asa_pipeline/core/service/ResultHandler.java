/*
 *

 */
package com.compomics.pride_asa_pipeline.core.service;

import com.compomics.pride_asa_pipeline.core.logic.SpectrumAnnotatorResult;
import com.compomics.pride_asa_pipeline.model.Modification;
import java.io.File;
import java.util.Set;

/**
 *
 * @author Niels Hulstaert
 */
public interface ResultHandler {
    
    /**
     * Writes the result of the annotation pipeline to file. 
     * 
     * @param spectrumAnnotatorResult the spectrum annotator result
     * @return the file that was written to
     */
    File writeResultToFile(SpectrumAnnotatorResult spectrumAnnotatorResult);
    
    /**
     * Reads the result file and returns the SpectrumAnnotatorResult.
     *
     * @param resultFile the spectrum annotation pipeline result
     * @return the spectrum annotator result
     */
    SpectrumAnnotatorResult readResultFromFile(File resultFile);
    
    /**
     * Writes the used modifications in the annotation pipeline to file. 
     * 
     * @param experimentAccession the experiment accession
     * @param usedModifications the used modifications
     */
    void writeUsedModificationsToFile(String experimentAccession, Set<Modification> usedModifications);
    
}
