/*
 *

 */
package com.compomics.pride_asa_pipeline.service;

import java.util.List;

/**
 *
 * @author Niels Hulstaert
 */
public interface DbSpectrumService extends SpectrumService {
    
    /**
     * Fill the spectrum cache with the specified spectrumids
     * @param aSpectrumidCacheList
     */
    void cacheSpectra(List<String> aSpectrumidCacheList);

    /**
     * Clear the current cache.
     */
    void clearCache();

}
