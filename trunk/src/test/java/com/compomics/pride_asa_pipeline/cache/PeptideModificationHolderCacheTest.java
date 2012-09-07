/*
 *

 */
package com.compomics.pride_asa_pipeline.cache;

import com.compomics.pride_asa_pipeline.model.AminoAcidSequence;
import com.compomics.pride_asa_pipeline.model.PeptideModificationHolder;
import com.compomics.pride_asa_pipeline.model.UnknownAAException;
import static junit.framework.Assert.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 *
 * @author Niels Hulstaert
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:springXMLConfig.xml")
public class PeptideModificationHolderCacheTest {

    @Autowired
    private Cache<String, PeptideModificationHolder> peptideModificationHolderCache;

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
