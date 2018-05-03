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
package com.compomics.pride_asa_pipeline.core.service.impl;

import com.compomics.pride_asa_pipeline.core.cache.Cache;
import com.compomics.pride_asa_pipeline.core.repository.SpectrumRepository;
import com.compomics.pride_asa_pipeline.core.service.SpectrumService;

import com.compomics.pride_asa_pipeline.model.Peak;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.log4j.Logger;

/**
 *
 * @author Niels Hulstaert
 */
public class SpectrumServiceImpl implements SpectrumService {

    private static final Logger LOGGER = Logger.getLogger(SpectrumServiceImpl.class);
    
    private SpectrumRepository spectrumRepository;
    private Cache<String, List<Peak>> spectrumPeaksCache;

    public SpectrumRepository getSpectrumRepository() {
        return spectrumRepository;
    }

    public void setSpectrumRepository(SpectrumRepository spectrumRepository) {
        this.spectrumRepository = spectrumRepository;
    }

    public Cache<String, List<Peak>> getSpectrumPeaksCache() {
        return spectrumPeaksCache;
    }

    public void setSpectrumPeaksCache(Cache<String, List<Peak>> spectrumPeaksCache) {
        this.spectrumPeaksCache = spectrumPeaksCache;
    }

    @Override
    public List<Peak> getSpectrumPeaksBySpectrumId(String spectrumId) {
        //check if the spectrum peaks can be found in the cache
        List<Peak> peaks = spectrumPeaksCache.getFromCache(spectrumId);

        //else get them from the database
        if (peaks == null) {
            peaks = new ArrayList<Peak>();

            double[] mzValues = spectrumRepository.getMzValuesBySpectrumId(spectrumId);
            double[] intensities = spectrumRepository.getIntensitiesBySpectrumId(spectrumId);

            for (int i = 0; i < mzValues.length; i++) {
                Peak peak = new Peak(mzValues[i], intensities[i]);
                peaks.add(peak);
            }

            //add spectrum peaks in cache
            spectrumPeaksCache.putInCache(spectrumId, peaks);
        }

        return peaks;
    }

    @Override
    public HashMap<Double, Double> getSpectrumPeakMapBySpectrumId(String spectrumId) {
        HashMap<Double, Double> peaks = new HashMap<>();

        double[] mzValues = spectrumRepository.getMzValuesBySpectrumId(spectrumId);
        double[] intensities = spectrumRepository.getIntensitiesBySpectrumId(spectrumId);

        for (int i = 0; i < mzValues.length; i++) {
            peaks.put(mzValues[i], intensities[i]);
        }

        return peaks;
    }

    @Override
    public void cacheSpectra(List<String> aSpectrumidCacheList) {
        Map<String, List<Peak>> lPeakMapsBySpectrumIdList = spectrumRepository.getPeakMapsBySpectrumIdList(aSpectrumidCacheList);
        Set<String> lSpectrumids = lPeakMapsBySpectrumIdList.keySet();
        for (String lSpectrumid : lSpectrumids) {
            spectrumPeaksCache.putInCache(lSpectrumid, lPeakMapsBySpectrumIdList.get(lSpectrumid));
        }
    }

    /**
     * Clear the current cache.
     */
    @Override
    public void clearCache() {
        spectrumPeaksCache.clearCache();
    }
}
