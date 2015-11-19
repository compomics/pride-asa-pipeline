package com.compomics.pride_asa_pipeline.core.logic.impl;

import com.compomics.pride_asa_pipeline.core.logic.ChoiceIterable;
import com.compomics.pride_asa_pipeline.core.logic.PeptideVariationsGenerator;
import com.compomics.pride_asa_pipeline.core.model.ModificationCombination;
import com.compomics.pride_asa_pipeline.model.AminoAcid;
import com.compomics.pride_asa_pipeline.model.Modification;
import com.compomics.pride_asa_pipeline.model.ModificationFacade;
import com.compomics.pride_asa_pipeline.model.ModifiedPeptide;
import com.compomics.pride_asa_pipeline.model.Peptide;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.apache.log4j.Logger;

/**
 * @author Florian Reisinger Date: 29-Sep-2009
 * @since 0.1
 */
public class PeptideVariationsGeneratorImpl implements PeptideVariationsGenerator {

    private static final Logger LOGGER = Logger.getLogger(PeptideVariationsGeneratorImpl.class);

    @Override
    public Set<ModifiedPeptide> generateVariations(Peptide precursor, Set<ModificationCombination> modifications) {
        Set<ModifiedPeptide> result = new HashSet<>();
        if (modifications != null) {
            //for each ModificationCombination generate all possible modified peptide variations
            //there may be more than one for each ModificationCombination, since the position of
            //a modification may not be determined. E.g. if one methionine oxidation is possible,
            //it may occur on any of the present methionines, and thus create multiple variations.
            for (ModificationCombination modificationCombination : modifications) {
                result.addAll(generateVariation(precursor, modificationCombination));
            }
        }
        return result;
    }

    private Set<ModifiedPeptide> generateVariation(Peptide precursor, ModificationCombination modificationCombination) {
        //we calculate the possible distribution combinations for each modification type and
        //store them in a set
        Set<Set<ModifiedPeptide>> modifiedPeptidesByModifications = new HashSet<>();
        //we only need to calculate the combinations for each unique modification type
        //(we don't want to do the same work multiple times) so we iterate over the unique set
        for (Modification modification : modificationCombination.getUniqueModifications()) {
            //get the number of occurances of the current modification type in the ModificationCombination
            int occurances = getNumberOfOccurances(modificationCombination, modification);
            modifiedPeptidesByModifications.add(variateModification(precursor, modification, occurances));
        }

        //now combine the variation sets for each modification type into a set of variations
        //for the whole ModificationCombination
        return combineVariationSets(modifiedPeptidesByModifications);
    }

    private Set<ModifiedPeptide> combineVariationSets(Set<Set<ModifiedPeptide>> modifiedPeptidesByModifications) {
        //check that we really have something to work with
        if (modifiedPeptidesByModifications == null || modifiedPeptidesByModifications.isEmpty()) {
            throw new IllegalArgumentException("Can not combine empty modified peptide sets!");
        }

        //if we only have one modification type, we can immediately return the according set of modified peptides
        if (modifiedPeptidesByModifications.size() == 1) {
            return modifiedPeptidesByModifications.iterator().next();
        }

        //need to combine every ModifiedPeptide from one set with each ModifiedPeptide
        //from the other sets to build the overlap sets. Also need to take into account
        //that only one modification can occur at a particular index.
        //Combine the first two sets and then combine the result with the next set and
        //the result of that with the following set, ...
        //ToDo: check the following
        Iterator<Set<ModifiedPeptide>> iterator = modifiedPeptidesByModifications.iterator();
        Set<ModifiedPeptide> combinedModifiedPeptides = iterator.next();
        Set<ModifiedPeptide> modifiedPeptidesByModification;
        while (iterator.hasNext()) {
            modifiedPeptidesByModification = iterator.next();
            combinedModifiedPeptides = combineVariations(combinedModifiedPeptides, modifiedPeptidesByModification);
        }

        return combinedModifiedPeptides;
    }

    private Set<ModifiedPeptide> combineVariations(Set<ModifiedPeptide> modPeptidesByModOne, Set<ModifiedPeptide> modPeptidesByModTwo) {
        //combine each ModifiedPeptide of the first set with all ModifiedPeptideS of the second set and
        //take into account (at least at the moment) we don't allow multiple non-terminal modifications
        //at the same locus.
        //ToDo: can be optimised!

        //since we combine modification sets for one ModificationCombination, we only expect a maximum
        //of one N-terminal and one C-terminal modification.
        //keep track of the N-terminal modification
        ModificationFacade nTermMofication = null;
        //keep track of the C-terminal modification
        ModificationFacade cTermModification = null;

        Set<ModifiedPeptide> combinedModifiedPeptides = new HashSet<>();
        //loop over first set
        for (ModifiedPeptide modPeptideByModOne : modPeptidesByModOne) {
            int numAA = modPeptideByModOne.length();
            //check the terminal modifications of the first set (only one modification type allowed across all sets!)
            if (nTermMofication == null) {
                nTermMofication = modPeptideByModOne.getNTermMod();
            } else if (modPeptideByModOne.getNTermMod() != null && nTermMofication != modPeptideByModOne.getNTermMod()) {
                throw new IllegalStateException("Can not combine ModifiedPeptide sets! Clashing N-terminal modifications.");
            }
            if (cTermModification == null) {
                cTermModification = modPeptideByModOne.getCTermMod();
            } else if (modPeptideByModOne.getCTermMod() != null && cTermModification != modPeptideByModOne.getCTermMod()) {
                throw new IllegalStateException("Can not combine ModifiedPeptide sets! Clashing C-terminal modifications.");
            }
            //loop over second set
            for (ModifiedPeptide modPeptideByModTwo : modPeptidesByModTwo) {
                if (numAA != modPeptideByModTwo.length()) {
                    throw new IllegalStateException("Attempt to combine two modified peptides with different length!");
                }
                //check the terminal modifications of the second set (cannot clash with the first set)
                if (nTermMofication == null) {
                    nTermMofication = modPeptideByModTwo.getNTermMod();
                } else if (modPeptideByModTwo.getNTermMod() != null && nTermMofication != modPeptideByModTwo.getNTermMod()) {
                    throw new IllegalStateException("Can not combine ModifiedPeptide sets! Clashing N-terminal modifications.");
                }
                if (cTermModification == null) {
                    cTermModification = modPeptideByModTwo.getCTermMod();
                } else if (modPeptideByModTwo.getCTermMod() != null && cTermModification != modPeptideByModTwo.getCTermMod()) {
                    throw new IllegalStateException("Can not combine ModifiedPeptide sets! Clashing C-terminal modifications.");
                }
                //so far so good, create a new combination and check the non-terminal modifications
                ModifiedPeptide combinedModifiedPeptide = new ModifiedPeptide(modPeptideByModOne.getUnmodifiedPeptide());
                if (nTermMofication != null) {
                    combinedModifiedPeptide.setNTermMod(nTermMofication);
                }
                if (cTermModification != null) {
                    combinedModifiedPeptide.setCTermMod(cTermModification);
                }

                //iterate over all non-terminal positions of the peptide sequence
                for (int i = 0; i < numAA; i++) {
                    //check the modifications of both ModifiedPeptides on the current position
                    //if there are none, then no modification is added to the combination
                    //if there is only one modification at this residue, then add it to the combination
                    //if there are two, then discard this combination (as - at least according to current
                    //    conventions - two modifications can not occur on the same residue at the same time)
                    ModificationFacade m1 = modPeptideByModOne.getNTModification(i);
                    ModificationFacade m2 = modPeptideByModTwo.getNTModification(i);

                    //m1 not null, so the first modified peptide carries a modification here
                    if (m1 != null) {
                        if (m2 == null) {
                            combinedModifiedPeptide.setNTModification(i, m1);
                        } else {
                            //clashing modifications, we have to disregard this combination
                            combinedModifiedPeptide = null;
                            break; //no need to check the other residues
                        }
                    } else {
                        //m2 not null, so the first modified peptide carries a modification here
                        if (m2 != null) {
                            combinedModifiedPeptide.setNTModification(i, m2);
                        }
                    }
                }

                //check that we have a sensible combination (e.g. not null and at least one modification)
                if (combinedModifiedPeptide != null && (combinedModifiedPeptide.getNumberNTModifications() > 0 || combinedModifiedPeptide.getNTermMod() != null || combinedModifiedPeptide.getCTermMod() != null)) {
                    combinedModifiedPeptides.add(combinedModifiedPeptide);
                } else {
                    //ToDo: maybe report the rubbish result
                    LOGGER.warn("Non-sensical modification combination in ModifiedPeptide.");
                }
                //go on to the next combination

            } //loop over second set
        } //loop over first set

        //return the found combinations
        return combinedModifiedPeptides;
    }

    private int getNumberOfOccurances(ModificationCombination modComb, Modification mod) {
        int result = 0;
        for (Modification modification : modComb.getModifications()) {
            if (modification.equals(mod)) {
                result++;
            }
        }
        return result;
    }

    private Set<ModifiedPeptide> variateModification(Peptide precursor, Modification modification, int occurances) {
        if (precursor == null || modification == null) {
            throw new IllegalStateException("Need precursor peptide and modification to generate peptide variations.");
        }
        Set<ModifiedPeptide> result = new HashSet<>();
        //check if we have terminal modifications (they are easy, as they are either present or not)
        if (modification.getLocation() == Modification.Location.N_TERMINAL) {
            ModifiedPeptide modifiedPeptide = new ModifiedPeptide(precursor);
            modifiedPeptide.setNTermMod(modification);
            result.add(modifiedPeptide);
        } else if (modification.getLocation() == Modification.Location.C_TERMINAL) {
            ModifiedPeptide modifiedPeptide = new ModifiedPeptide(precursor);
            modifiedPeptide.setCTermMod(modification);
            result.add(modifiedPeptide);
        } else {
            //location is non-terminal
            //check how many AA can be affected by this modification
            int numberOfModifiableLocations = countModifiableLocations(precursor, modification);
            if (numberOfModifiableLocations < occurances) {
                //we have more modifications of this kind than we have modifiable AAs??
                throw new IllegalStateException("Can not have more modifications than there are modifiable AAs.");
                //LOGGER.warn("Can not have more modifications than there are modifiable AAs.");
            } else if (numberOfModifiableLocations == occurances) {
                //all the affectable locations are affected (easy solution)
                //we only need to create one ModifiedPeptide were all the affectable AA are modified
                ModifiedPeptide modifiedPeptide = new ModifiedPeptide(precursor);
                for (int i = 0; i < precursor.length(); i++) {
                    if (modification.getAffectedAminoAcids().contains(precursor.getAA(i))) {
                        modifiedPeptide.setNTModification(i, modification);
                    }
                }
                result.add(modifiedPeptide);
            } else {
                //we have more affectable locations than actual number of modifications
                //so we have to compute the possible combinations.

                //numberOfModifiableLocations (n) = number of modifiable locations for the current modification
                //occurances (k)             = number of times the modification actually occurs
                Integer[] positionIndexes = getPositionIndexes(precursor, modification, numberOfModifiableLocations);

                //Number of possibilities:
                //      n!
                //-------------
                //k! * (n - k)!
                //the ChoiceIterable will iterate over the possibilities of above formular
                ChoiceIterable<Integer> choice = new ChoiceIterable<>(occurances, positionIndexes);
                for (Integer[] indexArray : choice) {
                    //one possible combination of the modifications
                    //create a new precursor variation with modificatios on all those locations
                    ModifiedPeptide modifiedPeptide = new ModifiedPeptide(precursor);
                    for (Integer index : indexArray) {
                        modifiedPeptide.setNTModification(index, modification);
                    }
                    result.add(modifiedPeptide);
                }

            } //end of munber of mod possible cases

        } //end of location cases

        return result;
    }

    private int countModifiableLocations(Peptide precursor, Modification modification) {
        int counter = 0;
        //check all amino acids in the precursor sequence, and if they
        //can be affected by the modification, increase the counter.
        for (AminoAcid aa : precursor.getSequence().getAASequence()) {
            if (modification.getAffectedAminoAcids().contains(aa)) {
                counter++;
            }
        }
        return counter;
    }

    private Integer[] getPositionIndexes(Peptide precursor, Modification modification, int modifiableLocations) {
        Integer[] positionIndexes = new Integer[modifiableLocations];

        int counter = 0;
        for (int i = 0; i < precursor.getSequence().length(); i++) {
            if (modification.getAffectedAminoAcids().contains(precursor.getSequence().getAA(i))) {
                positionIndexes[counter] = i;
                counter++;
            }
        }

        return positionIndexes;
    }
}
