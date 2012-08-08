/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pride_asa_pipeline.cache.impl;

import com.compomics.pride_asa_pipeline.cache.Cache;
import com.compomics.pride_asa_pipeline.config.PropertiesConfigurationHolder;
import com.compomics.pride_asa_pipeline.model.PeptideModificationHolder;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author niels
 */
public class PeptideModificationHolderCache extends LinkedHashMap<String, PeptideModificationHolder> implements Cache<String, PeptideModificationHolder> {

    //set maximum cache size from properties file
    private static final int MAXIMUM_CACHE_SIZE = PropertiesConfigurationHolder.getInstance().getInt("modification_cache.maximum_cache_size");
    
    /**
     * Puts the given PeptideModificationHolder in the cache. If the maximum
     * cache size is reached, the first added element is removed and replaced by
     * the given sequenceModificationSelection.
     *
     * @param aminoAcidSequenceString  the amino acid sequence string key
     * @param peptideModificationHolder the sequence modification selection value
     */
    @Override
    public void putInCache(String aminoAcidSequenceString, PeptideModificationHolder peptideModificationHolder) {
        this.put(aminoAcidSequenceString, peptideModificationHolder);
    }
    
    /**
     * Gets the PeptideModificationHolder by its key, the amino acid
     * sequence. If nothing is found, null is returned.
     *
     * @param aminoAcidSequenceString the amino acid sequence string key
     * @return the PeptideModificationHolder
     */
    @Override
    public PeptideModificationHolder getFromCache(String aminoAcidSequenceString) {
        return this.get(aminoAcidSequenceString);
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<String, PeptideModificationHolder> eldest) {
        return this.size() > MAXIMUM_CACHE_SIZE;
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
