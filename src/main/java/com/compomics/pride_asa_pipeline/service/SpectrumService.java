/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pride_asa_pipeline.service;

import com.compomics.pride_asa_pipeline.model.Peak;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author niels
 */
public interface SpectrumService {

    /**
     * Gets the spectrum peak list by spectrum ID.
     *
     * @param spectrumId the spectrum ID
     * @return the spectrum peaks
     */
    List<Peak> getSpectrumPeaksBySpectrumId(long spectrumId);

    /**
     * Gets the spectrum peak map (key: mz value, value: intensity value) by
     * spectrum ID.
     *
     * @param spectrumId the spectrum ID
     * @return the spectrum peaks
     */
    HashMap<Double, Double> getSpectrumPeakMapBySpectrumId(long spectrumId);

    void cacheSpectra(List<Long> aSpectrumidCacheList);

    Map getCachedSpectrum(Long aSpectrumid);
}
