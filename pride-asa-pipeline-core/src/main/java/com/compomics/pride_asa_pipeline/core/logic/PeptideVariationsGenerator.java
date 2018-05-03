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
package com.compomics.pride_asa_pipeline.core.logic;

import com.compomics.pride_asa_pipeline.core.model.ModificationCombination;
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
