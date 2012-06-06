/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pride_asa_pipeline.model;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author niels
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
