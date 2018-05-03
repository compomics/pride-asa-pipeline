/* 
 * Copyright 2018 compomics.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.compomics.pride_asa_pipeline.core.cache.impl;

import com.compomics.pride_asa_pipeline.core.cache.Cache;
import com.compomics.pride_asa_pipeline.core.config.PropertiesConfigurationHolder;
import com.compomics.pride_asa_pipeline.model.Peak;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Niels Hulstaert
 */
public class SpectrumPeaksCache extends LinkedHashMap<String, List<Peak>> implements Cache<String, List<Peak>> {
    
    private static final long serialVersionUID = 1L;

    /**
     * Puts the given spectrum peak list in the cache. If the maximum cache size
     * is reached, the first added element is removed and replaced by the given
     * peak list.
     *
     * @param spectrumId the spectrum ID
     * @param peaks the spectrum peak list
     */
    @Override
    public void putInCache(String spectrumId, List<Peak> peaks) {
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
    public List<Peak> getFromCache(String spectrumId) {
        return this.get(spectrumId);
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<String, List<Peak>> eldest) {
        return this.size() > PropertiesConfigurationHolder.getInstance().getInt("spectrum_peaks_cache.maximum_cache_size");
    }

    @Override
    public int getCacheSize() {
        return this.size();
    }

    @Override
    public void clearCache() {        
        this.clear();
    }

}
