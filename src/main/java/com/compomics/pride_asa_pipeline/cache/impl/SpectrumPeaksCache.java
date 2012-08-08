/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pride_asa_pipeline.cache.impl;

import com.compomics.pride_asa_pipeline.cache.Cache;
import com.compomics.pride_asa_pipeline.config.PropertiesConfigurationHolder;
import com.compomics.pride_asa_pipeline.model.Peak;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author niels
 */
public class SpectrumPeaksCache extends LinkedHashMap<Long, List<Peak>> implements Cache<Long, List<Peak>> {

    //set maximum cache size from properties file
    private int MAXIMUM_CACHE_SIZE = PropertiesConfigurationHolder.getInstance().getInt("spectrum_peaks_cache.maximum_cache_size");

    /**
     * Puts the given spectrum peak list in the cache. If the maximum cache size
     * is reached, the first added element is removed and replaced by the given
     * peak list.
     *
     * @param spectrumId the spectrum ID
     * @param peaks the spectrum peak list
     */
    @Override
    public void putInCache(Long spectrumId, List<Peak> peaks) {
        this.put(spectrumId, peaks);
    }

    /**
     * Gets the peak list by its key, spectrum ID. If nothing is found, null is
     * returned.
     *
     * @param spectrumId the spectrum ID
     * @return the spectrum peak list
     */
    @Override
    public List<Peak> getFromCache(Long spectrumId) {
        return this.get(spectrumId);
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<Long, List<Peak>> eldest) {
        return this.size() > MAXIMUM_CACHE_SIZE;
    }

    @Override
    public int getCacheSize() {
        return this.size();
    }

    @Override
    public void clearCache() {
        // Reset cache size.
        MAXIMUM_CACHE_SIZE = PropertiesConfigurationHolder.getInstance().getInt("spectrum_peaks_cache.maximum_cache_size");
        this.clear();
    }
}
