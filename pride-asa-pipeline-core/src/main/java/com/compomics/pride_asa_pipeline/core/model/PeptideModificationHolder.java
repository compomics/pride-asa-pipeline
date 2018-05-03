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
package com.compomics.pride_asa_pipeline.core.model;

import com.compomics.pride_asa_pipeline.model.Modification;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Niels Hulstaert
 */
public class PeptideModificationHolder {

    /**
     * The peptide amino acid sequence string.
     */
    private String aminoAcidSequenceString;
    /**
     * The possible modifications.
     */
    private List<Set<Modification>> possibleModifications;
    /**
     * The possible modification combinations
     */
    private HashSet<ModificationCombination> candidateModificationCombinations;

    public PeptideModificationHolder(String aminoAcidSequenceString) {
        this.aminoAcidSequenceString = aminoAcidSequenceString;        
    }

    public String getAminoAcidSequenceString() {
        return aminoAcidSequenceString;
    }        
    
    /**
     * Gets the possible modifications; n+2 sets of modifications, where n is the
     * amino acid sequence length. Each set contains the possible modifications
     * for one amino acid of the sequence. The first set contains the possible
     * N-terminal modifications, the last set contains the C-terminal
     * modifications.
     * 
     * @return the possible peptide modifications
     */    
    public List<Set<Modification>> getModifications() {
        return possibleModifications;
    }

    public void setPossibleModifications(List<Set<Modification>> possibleModifications) {
        this.possibleModifications = possibleModifications;
    }
        
    public HashSet<ModificationCombination> getCandidateModificationCombinations() {
        return candidateModificationCombinations;
    }

    public void setCandidateModificationCombinations(HashSet<ModificationCombination> candidateModificationCombinations) {
        this.candidateModificationCombinations = candidateModificationCombinations;
    }
                    
}
