/*
 *

 */
package com.compomics.pride_asa_pipeline.service;

import com.compomics.pride_asa_pipeline.model.Peak;

import java.util.HashMap;
import java.util.List;

/**
 *
 * @author Niels Hulstaert
 */
public interface SpectrumService {

    /**
     * Gets the spectrum peak list by spectrum ID.
     *
     * @param spectrumId the spectrum ID
     * @return the spectrum peaks
     */
    List<Peak> getSpectrumPeaksBySpectrumId(String spectrumId);

    /**
     * Gets the spectrum peak map (key: mz value, value: intensity value) by
     * spectrum ID.
     *
     * @param spectrumId the spectrum ID
     * @return the spectrum peaks
     */
    HashMap<Double, Double> getSpectrumPeakMapBySpectrumId(String spectrumId);    

}
