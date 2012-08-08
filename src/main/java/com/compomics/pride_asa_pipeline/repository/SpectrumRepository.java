/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pride_asa_pipeline.repository;

import com.compomics.pride_asa_pipeline.model.Peak;

import java.util.List;
import java.util.Map;

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

    /**
     * Gets Maps with spectrumid:peakMap structure
     *
     * @param spectrumIds the spectrum IDs
     * @return the Map with spectrumIds as keys, and Maps (mass:intensity) as values
     */
    Map<Long, List<Peak>> getPeakMapsBySpectrumIdList(List<Long> spectrumIds);

    
}
