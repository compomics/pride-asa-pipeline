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
package com.compomics.pride_asa_pipeline.core.cache;

import com.compomics.pride_asa_pipeline.core.cache.Cache;
import com.compomics.pride_asa_pipeline.core.cache.impl.PeptideModificationHolderCache;
import com.compomics.pride_asa_pipeline.model.AminoAcidSequence;
import com.compomics.pride_asa_pipeline.core.model.PeptideModificationHolder;
import com.compomics.pride_asa_pipeline.model.UnknownAAException;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author Niels Hulstaert
 */
public class PeptideModificationHolderCacheTest {

    private Cache<String, PeptideModificationHolder> peptideModificationHolderCache = new PeptideModificationHolderCache();

    @Test
    public void testCache() throws UnknownAAException {
        //clear cache to ensure we start with an empty cache
        peptideModificationHolderCache.clearCache();
        
        AminoAcidSequence aminoAcidSequence_1 = new AminoAcidSequence("AAABBB");
        AminoAcidSequence aminoAcidSequence_2 = new AminoAcidSequence("AAABCC");
        AminoAcidSequence aminoAcidSequence_3 = new AminoAcidSequence("AAABBB");
        AminoAcidSequence aminoAcidSequence_4 = new AminoAcidSequence("AAABEE");
        AminoAcidSequence aminoAcidSequence_5 = new AminoAcidSequence("AAABFF");
        AminoAcidSequence aminoAcidSequence_6 = new AminoAcidSequence("AAABGG");
        AminoAcidSequence aminoAcidSequence_7 = new AminoAcidSequence("AAABHH");
        AminoAcidSequence aminoAcidSequence_8 = new AminoAcidSequence("AAABII");
        AminoAcidSequence aminoAcidSequence_9 = new AminoAcidSequence("AAABKK");

        PeptideModificationHolder peptideModificationHolder = new PeptideModificationHolder(null);

        //put entries in cache
        peptideModificationHolderCache.putInCache(aminoAcidSequence_1.toString(), peptideModificationHolder);
        peptideModificationHolderCache.putInCache(aminoAcidSequence_2.toString(), peptideModificationHolder);
        peptideModificationHolderCache.putInCache(aminoAcidSequence_3.toString(), peptideModificationHolder);

        //AASequence_1 == AASequence_2, so the cache should only contain 2 entries
        assertEquals(2, peptideModificationHolderCache.getCacheSize());

        peptideModificationHolderCache.putInCache(aminoAcidSequence_4.toString(), peptideModificationHolder);
        peptideModificationHolderCache.putInCache(aminoAcidSequence_5.toString(), peptideModificationHolder);
        peptideModificationHolderCache.putInCache(aminoAcidSequence_6.toString(), peptideModificationHolder);
        peptideModificationHolderCache.putInCache(aminoAcidSequence_7.toString(), peptideModificationHolder);
        peptideModificationHolderCache.putInCache(aminoAcidSequence_8.toString(), peptideModificationHolder);
        peptideModificationHolderCache.putInCache(aminoAcidSequence_9.toString(), peptideModificationHolder);

        //the maximum cache size is 5, so the cache should only contain the 5 latest entries
        assertEquals(5, peptideModificationHolderCache.getCacheSize());
        assertNull(peptideModificationHolderCache.getFromCache(aminoAcidSequence_1.toString()));
        assertNull(peptideModificationHolderCache.getFromCache(aminoAcidSequence_2.toString()));
        assertNull(peptideModificationHolderCache.getFromCache(aminoAcidSequence_3.toString()));
        assertNull(peptideModificationHolderCache.getFromCache(aminoAcidSequence_4.toString()));
        assertNotNull(peptideModificationHolderCache.getFromCache(aminoAcidSequence_5.toString()));
        assertNotNull(peptideModificationHolderCache.getFromCache(aminoAcidSequence_6.toString()));
        assertNotNull(peptideModificationHolderCache.getFromCache(aminoAcidSequence_7.toString()));
        assertNotNull(peptideModificationHolderCache.getFromCache(aminoAcidSequence_8.toString()));
        assertNotNull(peptideModificationHolderCache.getFromCache(aminoAcidSequence_9.toString()));
    }
}
