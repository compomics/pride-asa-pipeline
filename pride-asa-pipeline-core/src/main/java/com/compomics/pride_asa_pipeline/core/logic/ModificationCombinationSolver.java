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
            
}
