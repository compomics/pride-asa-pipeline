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
import com.compomics.pride_asa_pipeline.core.model.PeptideModificationHolder;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author Niels Hulstaert
 */
public class PeptideModificationHolderCache extends LinkedHashMap<String, PeptideModificationHolder> implements Cache<String, PeptideModificationHolder> {
    
    private static final long serialVersionUID = 1L;   

    public PeptideModificationHolderCache() {
    }        
    
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
        return this.size() > PropertiesConfigurationHolder.getInstance().getInt("modification_cache.maximum_cache_size");
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
