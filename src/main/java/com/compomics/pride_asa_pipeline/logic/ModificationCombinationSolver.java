/*
 *

 */
package com.compomics.pride_asa_pipeline.logic;

import com.compomics.pride_asa_pipeline.model.ModificationCombination;
import com.compomics.pride_asa_pipeline.model.ModificationHolder;
import com.compomics.pride_asa_pipeline.model.Peptide;
import java.util.Set;

/**
 *
 * @author Niels Hulstaert
 */
public interface ModificationCombinationSolver {

    /**
     * Creates a Set of ModificationCombinations that can explain the given
     * mass. The given mass is explained if the mass of the
     * ModificationCombination is in the range [ (massToExplain - deviation) to
     * (massToExplain + deviation) ]. A usual value for the deviation would be
     * the instrument mass error for the precursor measurements. Note: this
     * implementation uses the ZenArcher algorithm to generate the combinations
     * of modification masses that could explain the given precursor mass delta.
     *
     * @param peptide the precursor to find modifications for.
     * @param bagSize the number of modifications to use.
     * @param massToExplain the mass to explain with the modifications.
     * @param deviation the deviation in which the massToExplain is deemed
     * explained.
     * @return a Set of ModificationCombinations that can explain the
     * massToExplain.
     */
    Set<ModificationCombination> findModificationCombinations(Peptide peptide, int bagSize, double massToExplain, double deviation);
    
    /**
     * Gets the modification holder
     * 
     * @return the modification holder
     */
    ModificationHolder getModificationHolder();
      
        
    /**
     * Sets the modification holder.
     * 
     * @param modificationHolder the modification holder
     */
    void setModificationHolder(ModificationHolder modificationHolder);
    
}
