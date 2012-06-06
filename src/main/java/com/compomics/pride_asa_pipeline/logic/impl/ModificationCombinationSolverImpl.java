package com.compomics.pride_asa_pipeline.logic.impl;

import com.compomics.pride_asa_pipeline.cache.Cache;
import com.compomics.pride_asa_pipeline.logic.ModificationCombinationSolver;
import com.compomics.pride_asa_pipeline.logic.ZenArcher;
import com.compomics.pride_asa_pipeline.model.*;
import java.util.*;
import org.apache.log4j.Logger;

/**
 * @author Florian Reisinger Date: 25-Aug-2009
 * @author Jonathan Rameseder
 * @since 0.1
 */
public class ModificationCombinationSolverImpl implements ModificationCombinationSolver {

    private static final Logger LOGGER = Logger.getLogger(ModificationCombinationSolverImpl.class);
    private ModificationHolder modificationHolder;
    private Cache<String, PeptideModificationHolder> peptideModificationHolderCache;
    private ZenArcher zenArcher;

    public ModificationCombinationSolverImpl() {
    }

    @Override
    public ModificationHolder getModificationHolder() {
        return modificationHolder;
    }

    public Cache getPeptideModificationHolderCache() {
        return peptideModificationHolderCache;
    }

    public void setPeptideModificationHolderCache(Cache<String, PeptideModificationHolder> peptideModificationHolderCache) {
        this.peptideModificationHolderCache = peptideModificationHolderCache;
    }

    public ZenArcher getZenArcher() {
        return zenArcher;
    }

    public void setZenArcher(ZenArcher zenArcher) {
        this.zenArcher = zenArcher;
    }

    @Override
    public void setModificationHolder(ModificationHolder modificationHolder) {
        if (modificationHolder == null || modificationHolder.getNumberOfModifications() < 1) {
            //no modifications to choose from!
            LOGGER.error("ERROR: no selection for all possible modifications has been provided!");
            throw new IllegalArgumentException("The provided ModificationSelection does not contain any modifications!");
        }
        this.modificationHolder = modificationHolder;
    }

    @Override
    public Set<ModificationCombination> findModificationCombinations(Peptide peptide, int bagSize, double massToExplain, double deviation) {
        LOGGER.debug("Finding modifications for percursor: " + peptide.getSequenceString());

        //check if we can increase the bag size (number of possible modifications) on this peptide
        //note: the max number of possible mods is: peptide length + 2, since each residue can hold
        //       one modification and there can be two additional terminal modifications.  
        if (bagSize > peptide.getSequence().length() + 2) {
            //the bag size is already bigger than the possible number of modifications on this peptide!
            //ToDo: maybe replace with exception and check at call time of method ??
            LOGGER.debug("bag sixe exceeded peptide length for peptide: " + peptide.getSequenceString());
            return null;
        }

        //the set that will hold the list of modification combinations
        HashSet<ModificationCombination> modificationCombinations = new HashSet<ModificationCombination>();

        //generates the PeptideModificationHolder for the given peptide.
        //For performance purposes, the previous PeptideModificationHolders are cached
        LOGGER.debug("Generating peptide modification holder for peptide " + peptide.getSequenceString());
        PeptideModificationHolder peptideModificationHolder = generatePeptideMoficationHolder(peptide.getSequence());

        //check if we got possible modifications for the current peptide
        if (peptideModificationHolder.getModifications() == null || peptideModificationHolder.getModifications().isEmpty()) {
            //might be the case that there can not be any modifications for a peptide
            //ToDo: is that a valid case? or are there always mods possible?
            LOGGER.info("no modifications possible for peptide: " + peptide.getSequence());
            return null;
        }

        if (peptideModificationHolder.getCandidateModificationCombinations().size() <= 0) {
            LOGGER.info("no modifications possible for peptide: " + peptide.getSequence());
        }

        //The zen archer will try and find combinations of PTMs that will explain the mass delta
        //(massToExplain) of a given peptide, where the massToExplain usually corresponds to
        //the (experimental mass - theoretical mass). The zen archer will scan in a given window
        //which is defined by: mass delta +/- (deviation * charge).
        zenArcher.setMinimalSum(massToExplain - peptide.getCharge() * deviation);
        zenArcher.setMaximalSum(massToExplain + peptide.getCharge() * deviation);

        //loop over all possible modification combinations for the given peptide
        for (ModificationCombination candidateModificationCombination : peptideModificationHolder.getCandidateModificationCombinations()) {
            Set<List<Double>> modificationCombinationMasses = new HashSet<List<Double>>();
            //The combinationSizeLimit is variable and unbounded. It will increase until convergence
            //is achieved in IdentificationMassDeltaExplainer, so we need to ensure that it doesn't 
            //go beyond the actual number of modifications possible on a given peptide.
            for (int i = 1; i <= bagSize && i <= peptideModificationHolder.getModifications().size(); i++) {
                //Get all the mass combinations that fit the search window for a given bag size
                //unless the bag size becomes bigger then there are possible modifications
                if (i <= candidateModificationCombination.getModificationMasses().length) {
                    Set<List<Double>> sizedModCombinationMasses = zenArcher.computeCombinations(candidateModificationCombination.getModificationMasses(), i);
                    // Keep track of all results for all bag sizes
                    if (sizedModCombinationMasses != null && sizedModCombinationMasses.size() > 0) {
                        modificationCombinationMasses.addAll(sizedModCombinationMasses);
                    }
                } else {
                    //bag size greater than number of possible modifications
                }
            }
            //now process the combination of masses generated by the ZenArcher
            if (modificationCombinationMasses.size() > 0) {
                //convert masses back to modifications
                for (List<Double> combinationMasses : modificationCombinationMasses) {
                    List<ModificationCombination> modificationCombinationList = mapMassesToModifications(combinationMasses, candidateModificationCombination);
                    for (ModificationCombination comb : modificationCombinationList) {
                        if (!modificationCombinations.contains(comb)) {
                            modificationCombinations.add(comb);
                        }
                    }
                }
            } else {
                //ToDo: report the ones where no mod comb was found
                //System.out.println("did not find modification combination for prec " + peptide.getSequenceString() + " with bag size " + bagSize);
            }
        }

        return modificationCombinations;
    }

    /**
     * This method will map the provided masses back to modifications. Note:
     * this assumes that the provided masses originate from the provided
     * combination of modifications. (to lower the chance for modifications that
     * have the same mass)
     *
     * @param combinationMasses the Collection of masses making up the precursor
     * mass delta.
     * @param candidateModificationCombination the ModificationCombination that
     * is the source of the masses.
     * @return a List<ModificationCombination> that corresponds to the provided
     * masses and is a combination of modifications specified in the provided
     * ModificationCombination.
     */
    private List<ModificationCombination> mapMassesToModifications(Collection<Double> combinationMasses, ModificationCombination candidateModificationCombination) {
        //since there can be multiple modifications with the same mass, we only take those
        //in to account that are provided in the original ModificationCombination (which
        //hopefully will lower the risk of one mass mapping to more than one modification).

        //the result will be a list of one (or potentially more than one) ModificationCombination
        //containing modifications that correspond to the provided masses.
        List<ModificationCombination> modificationCombinations = new ArrayList<ModificationCombination>();
        //we assume one ModComb will be enough. We will add the mapped modifications
        //to this one and only create more ModCombs if necessary.
        ModificationCombination initialModificationCombination = new ModificationCombination();
        modificationCombinations.add(initialModificationCombination);

        //get all modifications with the given mass from the originating ModificationCombination
        for (Double combinationMass : combinationMasses) {
            Set<Modification> mappedModifications = candidateModificationCombination.getModificationByMass(combinationMass);
            if (mappedModifications.size() < 1) {
                throw new IllegalStateException("The mass '" + combinationMass + "' is does not originate "
                        + "from the ModificationCombination: " + candidateModificationCombination.toString());
            } else if (mappedModifications.size() == 1) {
                //add the only mapped modification to the list of (possibly more than one) ModCombs
                addToModificationCombinations(modificationCombinations, mappedModifications.iterator().next());
            } else {
                //report that and then try to compensate
                LOGGER.warn("WARNING: more than one modifications map to the mass '" + combinationMass + "'. Trying to take all possibilities into account.");
                //whenever we have more than one possible modification for the mass,
                //we have to create a duplicate of the current ModComb(s) for each of the
                //possibilities and add one of the mapped mods each
                List<ModificationCombination> tempModificationCombinations = new ArrayList<ModificationCombination>();
                for (Modification mappedModification : mappedModifications) {
                    for (ModificationCombination modificationCombination : modificationCombinations) {
                        ModificationCombination tempModificationCombination = modificationCombination.duplicate();
                        tempModificationCombination.addModification(mappedModification);
                        tempModificationCombinations.add(tempModificationCombination);
                    }
                }
                modificationCombinations = tempModificationCombinations;
            }
        }

        return modificationCombinations;
    }

    private void addToModificationCombinations(List<ModificationCombination> modificationCombinations, Modification modification) {
        for (ModificationCombination combination : modificationCombinations) {
            combination.addModification(modification);
        }
    }

    /**
     * Generates a PeptideModificationHolder
     *
     * @param sequence the amino acid sequence for which to find all possible
     * modifications.
     * @return the peptide modification holder
     */
    private PeptideModificationHolder generatePeptideMoficationHolder(AminoAcidSequence sequence) {
        //check if the sequence modification selection of this amino acid sequence is found in the cache
        PeptideModificationHolder peptideModificationHolder = peptideModificationHolderCache.getFromCache(sequence.toString());

        if (peptideModificationHolder == null) {
            List<Set<Modification>> possibleModifications = new ArrayList<Set<Modification>>();

            //all terminal modifications possible for the N-terminus of the given sequence
            Set<Modification> ntMods = new HashSet<Modification>();
            Collection<Modification> tmpN = modificationHolder.getNterminalMods(sequence.getAA(0));
            if (tmpN != null) {
                ntMods.addAll(tmpN);
            }
            possibleModifications.add(ntMods);

            //all non-terminal modifications possible for the given amino acid sequence
            //(note: that these can also be present on the terminal amino acids
            //         independent of any terminal modifications)
            for (AminoAcid aa : sequence.getAASequence()) {
                Set<Modification> nonTMods = new HashSet<Modification>();
                Collection<Modification> tmpNN = modificationHolder.getNonTerminalMods(aa);
                if (tmpNN != null) {
                    nonTMods.addAll(tmpNN);
                }
                possibleModifications.add(nonTMods);
            }

            //all terminal modifications possible for the C-terminus of the given sequence
            Set<Modification> ctMods = new HashSet<Modification>();
            Collection<Modification> tmpC = modificationHolder.getCterminalMods(sequence.getAA(sequence.length() - 1));
            if (tmpC != null) {
                ctMods.addAll(tmpC);
            }
            possibleModifications.add(ctMods);

            peptideModificationHolder = new PeptideModificationHolder(sequence.toString());

            //set the possible modifications
            peptideModificationHolder.setPossibleModifications(possibleModifications);

            //generate the candidate modification combinations and add them to the PeptideModificationHolder
            LOGGER.debug("Generating candidate modification combination for peptide " + sequence.toString());
            generateCandidateModificationCombinations(peptideModificationHolder);

            //add to cache
            peptideModificationHolderCache.putInCache(sequence.toString(), peptideModificationHolder);
        }

        return peptideModificationHolder;
    }

    /**
     * Method to calculate all possible modification combinations for a
     * precursor.
     *
     * @param sequenceIndex index to iterate over each modifiable position in
     * the precursor, for internal (recursive) use. to be initialised with 0.
     * @param possibleModifications the list containing all possible
     * modifications for all positions of the precursor.
     * @param modificationCombinations the set that is filled by the recursive
     * method while being passed on from recursion to recursion. It will contain
     * the modification combinations possible for the current precursor.
     * @param candidateModifications List of modifications to be used internally
     * in recursion. To be initialised with an empty list.
     */
    private void calculateModificationCombinations(int sequenceIndex,
            List<Set<Modification>> possibleModifications,
            HashSet<ModificationCombination> modificationCombinations,
            List<Modification> candidateModifications) {
        //recursive method!

        //ToDo: the ModificationCombinations don't preserve the modification order (as possible in the precursor)!
        //ToDo: the list of modifications is sorted (for comparison purposes)!
        //ToDo: check if that may have effects later on (or if we could improve the algorithm if we preserve the order)
        if (possibleModifications.get(sequenceIndex) != null && possibleModifications.get(sequenceIndex).size() > 0) {
            //loop over all the modifications possible at a given AA position in the precursor,
            //as provided by allPossibilities
            for (Modification modification : possibleModifications.get(sequenceIndex)) {

                List<Modification> currentCandidateModifications = new ArrayList<Modification>();
                currentCandidateModifications.addAll(candidateModifications);
                currentCandidateModifications.add(modification);
                //recurse
                if (sequenceIndex < possibleModifications.size() - 1) {
                    calculateModificationCombinations(sequenceIndex + 1, possibleModifications, modificationCombinations, currentCandidateModifications);
                } else {
                    //if end of list reached, store candidates as new solutions
                    //need to sort the collection so that the equals method of ModificationCombination produces the
                    //correct result as it calls equals on a list internally
                    Collections.sort(currentCandidateModifications);
                    ModificationCombination modificationCombination = new ModificationCombination(currentCandidateModifications);
                    if (!modificationCombinations.contains(modificationCombination)) {
                        modificationCombinations.add(modificationCombination);
                    }
                }
            }
        } else {
            //if there are no possible modifications at the current position (sequenceIndex)
            //then we have to prevent the recursion from breaking
            if ((sequenceIndex + 1) < possibleModifications.size()) {
                calculateModificationCombinations(sequenceIndex + 1, possibleModifications, modificationCombinations, candidateModifications);
            } else {
                //if end of list reached, store candidates as new solutions
                //need to sort the collection so that the equals method of ModificationCombination produces the
                //correct result as it calls equals on a list internally
                Collections.sort(candidateModifications);
                ModificationCombination comb = new ModificationCombination(candidateModifications);
                if (!modificationCombinations.contains(comb)) {
                    modificationCombinations.add(comb);
                }
            }
        }
    }

    /**
     * Convenience wrapper around #calculateModificationCombinations.
     *
     * @param peptideModificationHolder the peptide modification holder
     */
    private void generateCandidateModificationCombinations(PeptideModificationHolder peptideModificationHolder) {
        HashSet<ModificationCombination> modificationCombinations = new HashSet<ModificationCombination>();
        calculateModificationCombinations(0, peptideModificationHolder.getModifications(), modificationCombinations, new ArrayList<Modification>());

        //store the possible modification combinations in the peptide modification holder
        peptideModificationHolder.setCandidateModificationCombinations(modificationCombinations);
    }
}
