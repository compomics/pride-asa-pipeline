/*
 *

 */
package com.compomics.pride_asa_pipeline.core.logic;

import com.compomics.pride_asa_pipeline.model.ModificationCombination;
import com.compomics.pride_asa_pipeline.model.ModifiedPeptide;
import com.compomics.pride_asa_pipeline.model.Peptide;
import java.util.Set;

/**
 *
 * @author Niels Hulstaert
 */
public interface PeptideVariationsGenerator {
    
    /**
     * This method will produce a set of 'modified peptides' which essentially
     * are multiple forms of the given precursor Peptide, each attached with a
     * different combination of the modifications specified in the provided
     * ModificationCombination.
     *
     * @param precursor the Peptide to generate the variations from.
     * @param modificationCombinations the ModificationCombination defining the allowed
     * modifications for the precursor.
     * @return a Set of ModifiedPeptide objects
     */
    Set<ModifiedPeptide> generateVariations(Peptide precursor, Set<ModificationCombination> modificationCombinations);
    
}
