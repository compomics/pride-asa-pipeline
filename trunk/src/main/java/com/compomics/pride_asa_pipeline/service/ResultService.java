/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pride_asa_pipeline.service;

import com.compomics.pride_asa_pipeline.model.SpectrumAnnotatorResult;

/**
 *
 * @author niels
 */
public interface ResultService {
    
    /**
     * Writes the result of the annotation pipeline to file. 
     * 
     * @param spectrumAnnotatorResult the spectrum annotator result
     */
    void writeResultToFile(SpectrumAnnotatorResult spectrumAnnotatorResult);
    
}
