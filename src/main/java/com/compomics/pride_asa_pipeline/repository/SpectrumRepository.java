/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pride_asa_pipeline.repository;

/**
 *
 * @author niels
 */
public interface SpectrumRepository {
    
    /**
     * Gets the spectrum mz values array as double array
     * 
     * @param spectrumId the spectrum ID
     * @return the mz values double array
     */
    double[] getMzValuesBySpectrumId(long spectrumId);    
    
    /**
     * Gets the spectrum intensities array as double array
     * 
     * @param spectrumId the spectrum ID
     * @return the intensities double array
     */
    double[] getIntensitiesBySpectrumId(long spectrumId);
    
}
