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
package com.compomics.pride_asa_pipeline.core.logic.spectrum.annotation.impl;

import com.compomics.pride_asa_pipeline.core.config.PropertiesConfigurationHolder;
import com.compomics.pride_asa_pipeline.core.logic.MassDeltaExplainer;
import com.compomics.pride_asa_pipeline.core.logic.PeptideVariationsGenerator;
import com.compomics.pride_asa_pipeline.core.logic.impl.MassDeltaExplainerImpl;
import com.compomics.pride_asa_pipeline.core.logic.recalibration.MassRecalibrator;
import com.compomics.pride_asa_pipeline.core.logic.spectrum.match.SpectrumMatcher;
import com.compomics.pride_asa_pipeline.core.model.MassRecalibrationResult;
import com.compomics.pride_asa_pipeline.core.model.ModificationCombination;
import com.compomics.pride_asa_pipeline.core.model.ModificationHolder;
import com.compomics.pride_asa_pipeline.core.model.ModifiedPeptidesMatchResult;
import com.compomics.pride_asa_pipeline.core.model.SpectrumAnnotatorResult;
import com.compomics.pride_asa_pipeline.core.service.ModificationService;
import com.compomics.pride_asa_pipeline.core.service.PipelineModificationService;
import com.compomics.pride_asa_pipeline.core.service.SpectrumService;
import com.compomics.pride_asa_pipeline.model.AASequenceMassUnknownException;
import com.compomics.pride_asa_pipeline.model.AnalyzerData;
import com.compomics.pride_asa_pipeline.model.AnnotationData;
import com.compomics.pride_asa_pipeline.model.Identification;
import com.compomics.pride_asa_pipeline.model.Identifications;
import com.compomics.pride_asa_pipeline.model.Modification;
import com.compomics.pride_asa_pipeline.model.ModifiedPeptide;
import com.compomics.pride_asa_pipeline.model.Peak;
import com.compomics.pride_asa_pipeline.model.Peptide;
import com.compomics.pride_asa_pipeline.model.PipelineExplanationType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.compomics.pride_asa_pipeline.core.gui.PipelineProgressMonitor;

/**
 *
 * @author Niels Hulstaert
 */
public abstract class SinglePassAbstractSpectrumAnnotator<T> {

    /**
     * The considered charge states.
     */
    protected Set<Integer> consideredChargeStates;
    /**
     * The experiment modifications.
     */
    protected Identifications identifications;
    /**
     * The pipeline modifications holder; contains all modifications considered
     * in the pipeline.
     */
    protected ModificationHolder modificationHolder;
    /**
     * The analyzer data.
     */
    protected AnalyzerData analyzerData;
    /**
     * The spectrum annotator result.
     */
    protected SpectrumAnnotatorResult spectrumAnnotatorResult;
    /**
     * Boolean that keeps track of the modifications state.
     */
    protected boolean areModificationsLoaded;
    /**
     * Beans.
     */
    protected MassRecalibrator massRecalibrator;
    protected SpectrumMatcher spectrumMatcher;
    protected MassDeltaExplainer massDeltaExplainer;
    protected PeptideVariationsGenerator peptideVariationsGenerator;
    protected SpectrumService spectrumService;
    protected PipelineModificationService pipelineModificationService;
    protected ModificationService modificationService;

    /**
     * Getters and setters.
     */
    public MassRecalibrator getMassRecalibrator() {
        return massRecalibrator;
    }

    public void setMassRecalibrator(MassRecalibrator massRecalibrator) {
        this.massRecalibrator = massRecalibrator;
    }

    public SpectrumMatcher getSpectrumMatcher() {
        return spectrumMatcher;
    }

    public void setSpectrumMatcher(SpectrumMatcher spectrumMatcher) {
        this.spectrumMatcher = spectrumMatcher;
    }

    public MassDeltaExplainer getMassDeltaExplainer() {
        return massDeltaExplainer;
    }

    public void setMassDeltaExplainer(MassDeltaExplainer massDeltaExplainer) {
        this.massDeltaExplainer = massDeltaExplainer;
    }

    public PeptideVariationsGenerator getPeptideVariationsGenerator() {
        return peptideVariationsGenerator;
    }

    public void setPeptideVariationsGenerator(PeptideVariationsGenerator peptideVariationsGenerator) {
        this.peptideVariationsGenerator = peptideVariationsGenerator;
    }

    public SpectrumService getSpectrumService() {
        return spectrumService;
    }

    public void setSpectrumService(SpectrumService spectrumService) {
        this.spectrumService = spectrumService;
    }

    public PipelineModificationService getPipelineModificationService() {
        return pipelineModificationService;
    }

    public void setPipelineModificationService(PipelineModificationService pipelineModificationService) {
        this.pipelineModificationService = pipelineModificationService;
    }

    public ModificationService getModificationService() {
        return modificationService;
    }

    public void setModificationService(ModificationService modificationService) {
        this.modificationService = modificationService;
    }

    public Identifications getIdentifications() {
        return identifications;
    }

    public SpectrumAnnotatorResult getSpectrumAnnotatorResult() {
        return spectrumAnnotatorResult;
    }

    public void setSpectrumAnnotatorResult(SpectrumAnnotatorResult spectrumAnnotatorResult) {
        this.spectrumAnnotatorResult = spectrumAnnotatorResult;
    }

    public ModificationHolder getModificationHolder() {
        return modificationHolder;
    }

    /**
     * Abstract methods.
     */
    /**
     * Loads the experiment identifications and calculates the systematic mass
     * errors per charge state.
     *
     * @param t the experiment identifier
     */
    public abstract void initIdentifications(T t);

    /**
     * Adds the pipeline modifications to the ModificationHolder and returns the
     * pride modifications as a set. If the pride modifications are not taken
     * into account, this set is empty.
     *
     * @return the modifications found in pride
     */
    public abstract Set<Modification> initModifications();

    /**
     * Clears the pipeline resources.
     */
    public abstract void clearPipeline();

    /**
     * Clears the temp files generated by the pipeline.
     */
    public abstract void clearTmpResources();

    /**
     * Public methods.
     */
    /**
     * Annotate the experiment identifications.
     */
    public void annotate() {
        if (!areModificationsLoaded) {
            //add the non-conflicting modifications found in pride for the given experiment
            Set<Modification> prideModifications = initModifications();
            if (!prideModifications.isEmpty()) {
                Set<Modification> conflictingModifications = modificationService.filterModifications(modificationHolder, prideModifications);
                for (Modification prideModification : prideModifications) {
                    if (!conflictingModifications.contains(prideModification)) {
                        modificationHolder.addModification(prideModification);
                    }
                }
            }
        }

        ///////////////////////////////////////////////////////////////////////
        //SECOND STEP: find all the modification combinations that could
        //              explain a given mass delta (if there is one) -> Zen Archer
        PipelineProgressMonitor.info("finding modification combinations");
        //set fragment mass error for the identification scorer
        Map<Identification, Set<ModificationCombination>> massDeltaExplanationsMap = findModificationCombinations(spectrumAnnotatorResult.getMassRecalibrationResult(), identifications);
        PipelineProgressMonitor.debug("Finished finding modification combinations");

        //the returned possibleExplanations map will contain all precursors for which a
        //possible explanation was found or which do not need to be explained (e.g. the
        //mass delta is smaller than the expected mass error)
        //-> the only precursors not in this map are those that carry a significant
        //modification, but nevertheless could not be explained!
        int explainedIdentificationsSize = massDeltaExplanationsMap.size();
        int completeIdentificationsSize = identifications.getCompleteIdentifications().size();
        PipelineProgressMonitor.debug("Precursors for which no modification combination could be found: " + (completeIdentificationsSize - explainedIdentificationsSize));
        List<Identification> unexplainedIdentifications = getUnexplainedIdentifications(identifications.getCompleteIdentifications(), massDeltaExplanationsMap.keySet());
        for (Identification identification : unexplainedIdentifications) {
            try {
                PipelineProgressMonitor.debug("Unresolved precursor: " + identification.getPeptide().toString() + " with mass delta: " + identification.getPeptide().calculateMassDelta());
            } catch (AASequenceMassUnknownException e) {
                PipelineProgressMonitor.error(e.getMessage(), e);
            }
        }
        spectrumAnnotatorResult.setUnexplainedIdentifications(unexplainedIdentifications);

        //create new map with only the precursors that carry a significant mass delta
        //and were we have possible modification combinations
        List<Identification> unmodifiedPrecursors = new ArrayList<>();
        Map<Identification, Set<ModificationCombination>> significantMassDeltaExplanationsMap = new HashMap<>();
        for (Identification identification : massDeltaExplanationsMap.keySet()) {
            if (massDeltaExplanationsMap.get(identification) != null) {
                significantMassDeltaExplanationsMap.put(identification, massDeltaExplanationsMap.get(identification));
            } else {
                identification.setPipelineExplanationType(PipelineExplanationType.UNMODIFIED);
                unmodifiedPrecursors.add(identification);

                //annotate the unmodified identifications
                //score the unmodified identification
                AnnotationData annotationData = spectrumMatcher.matchPrecursor(identification.getPeptide(), spectrumService.getSpectrumPeaksBySpectrumId(identification.getSpectrumId()), analyzerData.getFragmentMassError());
                identification.setAnnotationData(annotationData);
            }
        }
        spectrumAnnotatorResult.setUnmodifiedPrecursors(unmodifiedPrecursors);
        PipelineProgressMonitor.debug("Precursors with possible modification(s): " + significantMassDeltaExplanationsMap.size());
        PipelineProgressMonitor.debug("Precursors with mass delta smaller than mass error (probably unmodified): " + unmodifiedPrecursors.size());

        ///////////////////////////////////////////////////////////////////////
        //THIRD STEP:  find all possible precursor variations (taking all
        //              the possible modification combinations into account)
        //ToDo: the following approach very quickly evolves into a combinatorial explosion,
        //ToDo: we should find better ways to determine which combination of modification
        //ToDo: is best suitable to explain a spectrum.
        //ToDo: Maybe looking at the spectrum early on to eliminate some combinations or
        //ToDo: to get ideas about likely explanations would help?
        PipelineProgressMonitor.info("finding precursor variations");
        Map<Identification, Set<ModifiedPeptide>> modifiedPrecursorVariations = findPrecursorVariations(significantMassDeltaExplanationsMap);
        PipelineProgressMonitor.debug("finished finding precursor variations");
        //For each of these 'variations' we then calculate all possible fragment ions.

        ///////////////////////////////////////////////////////////////////////
        //FOURTH STEP:  create theoretical fragment ions for all precursors
        //               match them onto the peaks in the spectrum and decide
        //               which one is the best 'explanation'
        PipelineProgressMonitor.info("finding best matches");
        List<Identification> modifiedPrecursors = findBestMatches(modifiedPrecursorVariations);
        PipelineProgressMonitor.debug("finished finding best matches");
        spectrumAnnotatorResult.setModifiedPrecursors(modifiedPrecursors);
    }

    /**
     * Private and protected methods
     */
    /**
     * finds the systematic mass errors per charge state.
     *
     * @param completePeptides list of complete peptides (i.e. all sequence AA
     * masses known)
     * @return the mass recalibration result (systemic mass error) per charge
     * state
     */
    protected MassRecalibrationResult findSystematicMassError(List<Peptide> completePeptides) {
        //set considered charge states
        massRecalibrator.setConsideredChargeStates(consideredChargeStates);

        MassRecalibrationResult massRecalibrationResult = null;
        try {
            massRecalibrationResult = massRecalibrator.recalibrate(analyzerData, completePeptides);
        } catch (AASequenceMassUnknownException e) {
            //this should not happen here, since we only handle 'complete precursors' where
            //all the amino acids have a known mass
            PipelineProgressMonitor.error("ERROR! Could not calculate masses for all (complete) precursors!");
            throw new IllegalStateException("Could not calculate masses for all (complete) precursors!");
        }
        return massRecalibrationResult;
    }

    protected void initChargeStates() {
        //load default values for considered charge states
        consideredChargeStates = new HashSet<>();
        for (Object chargeState : PropertiesConfigurationHolder.getInstance().getList("massrecalibrator.considered_charge_states")) {
            consideredChargeStates.add(Integer.parseInt(chargeState.toString()));
        }
    }

    /**
     * finds all possible modifications that could explain a mass delta -> Zen
     * Archer.
     *
     * @param massRecalibrationResult the mass recalibration result (systemic
     * mass error) per charge state
     * @param identifications the identifications
     * @return the possible modifications map (key: the identification data,
     * value the set of modification combinations)
     */
    private Map<Identification, Set<ModificationCombination>> findModificationCombinations(MassRecalibrationResult massRecalibrationResult, Identifications identifications) {
        Map<Identification, Set<ModificationCombination>> possibleExplanations = new HashMap<>();

        //check if the modification holder contains at least one modification
        if (!modificationHolder.getAllModifications().isEmpty()) {
            massDeltaExplainer = new MassDeltaExplainerImpl(modificationHolder);
            //finally calculate the possible explanations
            possibleExplanations = massDeltaExplainer.explainCompleteIndentifications(identifications.getCompleteIdentifications(), massRecalibrationResult, analyzerData);
        }

        return possibleExplanations;
    }

    /**
     * find all possible precursor variations (taking all the possible
     * modification combinations into account).
     *
     * @param massDeltaExplanationsMap the possible modifications map (key: the
     * identification data, value the set of modification combinations)
     * @return the precursor variations map (key: the identification data, value
     * the set of modification combinations)
     */
    private Map<Identification, Set<ModifiedPeptide>> findPrecursorVariations(Map<Identification, Set<ModificationCombination>> possibleExplanations) {
        //From the theoretical information we already have (e.g. the precursor sequence
        //and all possible modifications) we therefore first create all possible
        //'modified precursors' or 'precursor variations' (e.g. the same peptide sequence,
        //but with different modifications on different locations).
        Map<Identification, Set<ModifiedPeptide>> precursorVariations = new HashMap<>();
        for (Identification identificationSet : possibleExplanations.keySet()) {
            Set<ModificationCombination> modifications = possibleExplanations.get(identificationSet);
            Set<ModifiedPeptide> precursorVariationsSet = peptideVariationsGenerator.generateVariations(identificationSet.getPeptide(), modifications);
            precursorVariations.put(identificationSet, precursorVariationsSet);
        }
        PipelineProgressMonitor.debug("Peptide variations found for " + precursorVariations.size() + " peptides.");

        return precursorVariations;
    }

    /**
     * creates theoretical fragment ions for all precursors match them onto the
     * peaks in the spectrum and decide which one is the best 'explanation'.
     *
     * @param experimentAccession the experiment accession number
     * @param modifiedPrecursorVariationsMap the modified precursor variations
     * map (key: the identification data, value the set of modification
     * combinations)
     * @return the result list of identifications containing a modified peptide
     */
    private List<Identification> findBestMatches(Map<Identification, Set<ModifiedPeptide>> precursorVariations) {
        //After we now have all the theoretical ions for all possible 'precursor variations',
        //we can try and find them in the real spectrum. This will give an indication as to
        //which 'variation' of the precursor is the most likely one.

        //for each precursor: try all ModifiedPeptide variations
        //for all these variations: map against the spectra and assign a score
        List<Identification> bestMatches = new ArrayList<>();
        for (Identification identification : precursorVariations.keySet()) {
            //load spectrum
            List<Peak> peaks = spectrumService.getSpectrumPeaksBySpectrumId(identification.getSpectrumId());
            ModifiedPeptidesMatchResult modifiedPeptidesMatchResult = spectrumMatcher.findBestModifiedPeptideMatch(identification.getPeptide(), precursorVariations.get(identification), peaks, analyzerData.getFragmentMassError());
            if (modifiedPeptidesMatchResult != null) {
                identification.setPeptide(modifiedPeptidesMatchResult.getModifiedPeptide());
                identification.setAnnotationData(modifiedPeptidesMatchResult.getAnnotationData());
                identification.setPipelineExplanationType(PipelineExplanationType.MODIFIED);
                bestMatches.add(identification);
            } else {
                PipelineProgressMonitor.info("No best match found for precursor: " + identification.getPeptide());
                //add to unexplained identifications
                identification.setPipelineExplanationType(PipelineExplanationType.UNEXPLAINED);
                spectrumAnnotatorResult.getUnexplainedIdentifications().add(identification);
            }
        }

        return bestMatches;
    }

    /**
     * Gets the identifications that where the mass delta couldn't be explained
     * by combining modifications.
     *
     * @param identifications all the experiment identifications
     * @param explainableIdentifications the explained identifications
     * @return the unexplained identifications
     */
    private List<Identification> getUnexplainedIdentifications(List<Identification> identifications, Set<Identification> explainableIdentifications) {
        List<Identification> unexplainedIdentifications = new ArrayList<>();
        for (Identification identification : identifications) {
            if (!explainableIdentifications.contains(identification)) {
                //annotate the unexplained identifications
                //score the unexplained identification
                AnnotationData annotationData = spectrumMatcher.matchPrecursor(identification.getPeptide(), spectrumService.getSpectrumPeaksBySpectrumId(identification.getSpectrumId().replace("index=", "")), analyzerData.getFragmentMassError());
                identification.setAnnotationData(annotationData);
                identification.setPipelineExplanationType(PipelineExplanationType.UNEXPLAINED);

                unexplainedIdentifications.add(identification);
            }
        }
        return unexplainedIdentifications;
    }
}
