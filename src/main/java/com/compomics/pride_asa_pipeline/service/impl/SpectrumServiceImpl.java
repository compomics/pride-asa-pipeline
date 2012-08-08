/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pride_asa_pipeline.service.impl;

import com.compomics.pride_asa_pipeline.cache.Cache;
import com.compomics.pride_asa_pipeline.model.Peak;
import com.compomics.pride_asa_pipeline.repository.SpectrumRepository;
import com.compomics.pride_asa_pipeline.service.SpectrumService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;

/**
 *
 * @author niels
 */
public class SpectrumServiceImpl implements SpectrumService {
    private static final Logger LOGGER = Logger.getLogger(SpectrumServiceImpl.class);

    private SpectrumRepository spectrumRepository;
    private Cache<Long, List<Peak>> spectrumPeaksCache;
    private Map<Long, Map> spectrumCache = null;

    public SpectrumRepository getSpectrumRepository() {
        return spectrumRepository;
    }

    public void setSpectrumRepository(SpectrumRepository spectrumRepository) {
        this.spectrumRepository = spectrumRepository;
    }

    public Cache<Long, List<Peak>> getSpectrumPeaksCache() {
        return spectrumPeaksCache;
    }

    public void setSpectrumPeaksCache(Cache<Long, List<Peak>> spectrumPeaksCache) {
        this.spectrumPeaksCache = spectrumPeaksCache;
    }

    @Override
    public List<Peak> getSpectrumPeaksBySpectrumId(long spectrumId) {
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
        else{
            LOGGER.debug("found spectrum with ID: " + spectrumId + " in cache.");
        }

        return peaks;
    }

    @Override
    public HashMap<Double, Double> getSpectrumPeakMapBySpectrumId(long spectrumId) {
        HashMap<Double, Double> peaks = new HashMap<Double, Double>();

        double[] mzValues = spectrumRepository.getMzValuesBySpectrumId(spectrumId);
        double[] intensities = spectrumRepository.getIntensitiesBySpectrumId(spectrumId);

        for (int i = 0; i < mzValues.length; i++) {
            peaks.put(mzValues[i], intensities[i]);
        }

        return peaks;
    }

    @Override
    public void cacheSpectra(List<Long> aSpectrumidCacheList) {
        spectrumCache = spectrumRepository.getPeakMapsBySpectrumIdList(aSpectrumidCacheList);
    }

    @Override
    public Map getCachedSpectrum(Long aSpectrumid) {
        return spectrumCache.get(aSpectrumid);
    }
}
