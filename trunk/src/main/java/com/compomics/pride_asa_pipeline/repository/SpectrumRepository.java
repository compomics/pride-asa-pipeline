/*
 *

 */
package com.compomics.pride_asa_pipeline.repository;

import com.compomics.pride_asa_pipeline.model.Peak;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Niels Hulstaert
 */
public interface SpectrumRepository {
    
    /**
     * Gets the spectrum mz values array as double array
     * 
     * @param spectrumId the spectrum ID
     * @return the mz values double array
     */
    double[] getMzValuesBySpectrumId(String spectrumId);    
    
    /**
     * Gets the spectrum intensities array as double array
     * 
     * @param spectrumId the spectrum ID
     * @return the intensities double array
     */
    double[] getIntensitiesBySpectrumId(String spectrumId);

    /**
     * Gets Maps with spectrumid:peakMap structure
     *
     * @param spectrumIds the spectrum IDs
     * @return the Map with spectrumIds as keys, and Maps (mass:intensity) as values
     */
    Map<String, List<Peak>> getPeakMapsBySpectrumIdList(List<String> spectrumIds);

    
}
