/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pride_asa_pipeline.service;

import com.compomics.pride_asa_pipeline.model.SpectrumAnnotatorResult;
import java.io.File;

/**
 *
 * @author Niels Hulstaert
 */
public interface ResultHandler {
    
    /**
     * Writes the result of the annotation pipeline to file. 
     * 
     * @param spectrumAnnotatorResult the spectrum annotator result
     */
    void writeResultToFile(SpectrumAnnotatorResult spectrumAnnotatorResult);
    
    /**
     * Reads the result file and returns the SpectrumAnnotatorResult
     *
     * @param resultFile the spectrum annotation pipeline result
     * @return the spectrum annotator result
     */
    SpectrumAnnotatorResult readResultFromFile(File resultFile);
    
}
