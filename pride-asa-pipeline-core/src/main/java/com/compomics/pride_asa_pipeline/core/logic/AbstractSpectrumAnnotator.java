package com.compomics.pride_asa_pipeline.core.logic;

import com.compomics.pride_asa_pipeline.core.cache.ParserCache;
import com.compomics.pride_asa_pipeline.core.config.PropertiesConfigurationHolder;
import com.compomics.pride_asa_pipeline.core.logic.impl.MassDeltaExplainerImpl;
import com.compomics.pride_asa_pipeline.core.logic.modification.InputType;
import com.compomics.pride_asa_pipeline.core.logic.recalibration.MassRecalibrator;
import com.compomics.pride_asa_pipeline.core.logic.spectrum.match.SpectrumMatcher;
import com.compomics.pride_asa_pipeline.core.model.MassRecalibrationResult;
import com.compomics.pride_asa_pipeline.core.model.ModificationCombination;
import com.compomics.pride_asa_pipeline.core.model.ModificationHolder;
import com.compomics.pride_asa_pipeline.core.model.ModifiedPeptidesMatchResult;
import com.compomics.pride_asa_pipeline.core.model.SpectrumAnnotatorResult;
import com.compomics.pride_asa_pipeline.core.model.modification.impl.AsapModificationAdapter;
import com.compomics.pride_asa_pipeline.core.model.modification.source.AnnotatedModificationService;
import com.compomics.pride_asa_pipeline.core.model.modification.source.PRIDEModificationFactory;
import com.compomics.pride_asa_pipeline.core.repository.impl.file.FileModificationRepository;
import com.compomics.pride_asa_pipeline.core.service.ModificationService;
import com.compomics.pride_asa_pipeline.core.service.PipelineModificationService;
import com.compomics.pride_asa_pipeline.core.service.SpectrumService;
import com.compomics.pride_asa_pipeline.model.AASequenceMassUnknownException;
import com.compomics.pride_asa_pipeline.model.AminoAcid;
import com.compomics.pride_asa_pipeline.model.AnalyzerData;
import com.compomics.pride_asa_pipeline.model.AnnotationData;
import com.compomics.pride_asa_pipeline.model.Identification;
import com.compomics.pride_asa_pipeline.model.Identifications;
import com.compomics.pride_asa_pipeline.model.Modification;
import com.compomics.pride_asa_pipeline.model.ModifiedPeptide;
import com.compomics.pride_asa_pipeline.model.Peak;
import com.compomics.pride_asa_pipeline.model.Peptide;
import com.compomics.pride_asa_pipeline.model.PipelineExplanationType;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import org.apache.log4j.Logger;
import org.springframework.core.io.Resource;

/**
 * @author Kenneth Verheggen
 */
public abstract class AbstractSpectrumAnnotator<T> {

    /**
     * The logger
     */
    private static final Logger LOGGER = Logger.getLogger(AbstractSpectrumAnnotator.class);
    /**
     * The maximal amount of mods that can be included per pass
     */
    private int MAX_PASS_SIZE = 6;
    /**
     * The maximal amount of mods that can be included per pass
     */
    private int MAX_MOD_ALLOWED = 6;
    /**
     * The maximal amount of mods that can be included per pass
     */
    private double target_explanation_ratio = 0.9;
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
     * Boolean that keeps track of the modifications state.
     */
    protected double courseFragmentAccuraccy = 1.0;
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

    public double getCourseFragmentAccuraccy() {
        return courseFragmentAccuraccy;
    }

    public void setCourseFragmentAccuraccy(double courseFragmentAccuraccy) {
        this.courseFragmentAccuraccy = courseFragmentAccuraccy;
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
     * @param modificationsResource the modifications resource
     * @param inputType the type of modifications resource
     * @return the modifications found in pride
     */
    public abstract Set<Modification> initModifications(Resource modificationsResource, InputType inputType);

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
     * Annotate the experiment identifications in multiple passes according to
     * the modification prevalence in PRIDE.
     */
    public void annotate(String... identifier) {
        if (identifier == null) {
            throw new IllegalArgumentException("Identifier can not be null : expected assay accession");
        }

        try {
            String assayAccession = identifier[0];
            LinkedHashSet<Modification> sortedAnnotatedModifications = new LinkedHashSet<>();
            //load the modifications from the PRIDE annotation
            if (ParserCache.getInstance().containsParser(assayAccession)) {
                FileModificationRepository repository = new FileModificationRepository();
                sortedAnnotatedModifications.addAll(repository.getModificationsByExperimentId(assayAccession));
            } else {
                AnnotatedModificationService annotatedModService = new AnnotatedModificationService();
                AsapModificationAdapter adapter = new AsapModificationAdapter();
                //get other modifications
                for (String aPTMName : annotatedModService.getAssayAnnotatedPTMs(assayAccession)) {
                    sortedAnnotatedModifications.add((Modification) PRIDEModificationFactory.getInstance().getModification(adapter, aPTMName));

                }
            }
            //order the annotated modifications to prevalence (in case there are more than the selected batch size)
            //get all asap mods
            LinkedList<Modification> sortedAllModifications = PRIDEModificationFactory.getAsapMods();
            LOGGER.info("There are " + sortedAnnotatedModifications.size() + " annotated mods out of " + sortedAllModifications.size() + " total.");
            //get a queue of them
            BlockingQueue<Modification> modQueue = new ArrayBlockingQueue<>(sortedAllModifications.size());
            //first get the annotated modifications and order those as well?
            for (Object modObject : sortedAnnotatedModifications) {
                Modification mod = (Modification) modObject;
                LOGGER.info("Annotated mod : " + mod.getName());
                modQueue.offer(mod);
                sortedAllModifications.remove(mod);
            }
            //add the others
            for (Modification mod : sortedAllModifications) {
                modQueue.offer(mod);
            }
            //drain the queue in subset parts
            Set<Modification> modPassSet = new HashSet<>();
            List<Identification> completeIdentifications = identifications.getCompleteIdentifications();
            List<Identification> unmodifiedPrecursors = new ArrayList<>();
            List<Identification> modifiedPrecursors = new ArrayList<>();
            List<Identification> unexplainedIdentifications = new ArrayList<>();
            //keep track of what needs to be processed
            List<Identification> identificationsToProcess = new ArrayList<>();
            identificationsToProcess.addAll(completeIdentifications);
            //create new map with only the precursors that carry a significant mass delta
            //and were we have possible modification combinations
            Map<Identification, Set<ModificationCombination>> significantMassDeltaExplanationsMap = new HashMap<>();
    
            int pass = 0;
            //subset the modQueue in x partitions
            LOGGER.info("Subsetting the modqueue (size=" + modQueue.size() + ") into " + (int) (modQueue.size() / MAX_PASS_SIZE) + " subsets");
            while ((modQueue.drainTo(modPassSet, MAX_PASS_SIZE) > 0)) {
                        modificationHolder = new ModificationHolder();

                //special cases such as oxidation, co-occurence of impossible mods,...
                if ((unexplainedIdentifications.isEmpty() && pass > 0)) {
                    LOGGER.info("No more unexplained identifications !");
                    break;
                } else if (((double) unexplainedIdentifications.size() / (double) completeIdentifications.size()) >= target_explanation_ratio) {
                    LOGGER.info(target_explanation_ratio * 100 + "% of identifications were explained !");
                    break;
                } else if (modificationHolder.getAllModifications().size() >= MAX_MOD_ALLOWED) {
                    LOGGER.info(MAX_MOD_ALLOWED + " is the highest amount of allowed modifications.");
                    break;
                } else {
                    pass++;
                    LOGGER.info("Searching " + pass + "th pass :" + modPassSet);

                    annotate(convertToUseCase(modPassSet),
                            identificationsToProcess,
                            modifiedPrecursors,
                            unmodifiedPrecursors,
                            completeIdentifications,
                            unexplainedIdentifications,
                            significantMassDeltaExplanationsMap);
                    modPassSet.clear();
                }
            }
            //set the master results into the spectrumAnnotator result
            spectrumAnnotatorResult.setUnexplainedIdentifications(unexplainedIdentifications);
            spectrumAnnotatorResult.setUnmodifiedPrecursors(unmodifiedPrecursors);
            spectrumAnnotatorResult.setModifiedPrecursors(modifiedPrecursors);
        } catch (IOException ex) {
            ex.printStackTrace();
            LOGGER.error(ex);
        }
    }

    private void annotate(Set<Modification> prideModifications,
            List<Identification> identificationsToProcess,
            List<Identification> modifiedPrecursors,
            List<Identification> unmodifiedPrecursors,
            List<Identification> unexplainedModifications,
            List<Identification> unexplainedIdentifications,
            Map<Identification, Set<ModificationCombination>> significantMassDeltaExplanationsMap) {
        Map<Identification, Set<ModificationCombination>> tempSignificantMassDeltaExplanationsMap = new HashMap<>();
        //add the non-conflicting modifications found in pride for the given experiment
        if (!prideModifications.isEmpty()) {
            Set<Modification> conflictingModifications = modificationService.filterModifications(modificationHolder, prideModifications);
            for (Modification prideModification : prideModifications) {
                if (!conflictingModifications.contains(prideModification)) {
                    modificationHolder.addModification(prideModification);
                }
            }
        }
        ///////////////////////////////////////////////////////////////////////
        //SECOND STEP: find all the modification combinations that could
        //              explain a given mass delta (if there is one) -> Zen Archer
        LOGGER.info("finding modification combinations");
        //set fragment mass error for the identification scorer
        Map<Identification, Set<ModificationCombination>> massDeltaExplanationsMap = findModificationCombinations(spectrumAnnotatorResult.getMassRecalibrationResult(), identificationsToProcess);
        identificationsToProcess.removeAll(massDeltaExplanationsMap.keySet());
        LOGGER.debug("Finished finding modification combinations");
        //the returned possibleExplanations map will contain all precursors for which a
        //possible explanation was found or which do not need to be explained (e.g. the
        //mass delta is smaller than the expected mass error)
        //-> the only precursors not in this map are those that carry a significant
        //modification, but nevertheless could not be explained!
        unexplainedIdentifications.addAll(getUnexplainedIdentifications(unexplainedModifications, tempSignificantMassDeltaExplanationsMap.keySet()));

        for (Identification identification : massDeltaExplanationsMap.keySet()) {
            if (tempSignificantMassDeltaExplanationsMap.get(identification) != null) {
                tempSignificantMassDeltaExplanationsMap.put(identification, tempSignificantMassDeltaExplanationsMap.get(identification));
            } else {
                identification.setPipelineExplanationType(PipelineExplanationType.UNMODIFIED);
                unmodifiedPrecursors.add(identification);
                //annotate the unmodified identifications
                AnnotationData annotationData = spectrumMatcher.matchPrecursor(identification.getPeptide(), spectrumService.getSpectrumPeaksBySpectrumId(identification.getSpectrumId()), courseFragmentAccuraccy);
                identification.setAnnotationData(annotationData);
            }
        }

        LOGGER.debug("Precursors with possible modification(s): " + tempSignificantMassDeltaExplanationsMap.size());
        LOGGER.debug("Precursors with mass delta smaller than mass error (probably unmodified): " + unmodifiedPrecursors.size());

        ///////////////////////////////////////////////////////////////////////
        //THIRD STEP:  find all possible precursor variations (taking all
        //              the possible modification combinations into account)
        //ToDo: the following approach very quickly evolves into a combinatorial explosion,
        //ToDo: we should find better ways to determine which combination of modification
        //ToDo: is best suitable to explain a spectrum.
        //ToDo: Maybe looking at the spectrum early on to eliminate some combinations or
        //ToDo: to get ideas about likely explanations would help?
        LOGGER.info("finding precursor variations");
        Map<Identification, Set<ModifiedPeptide>> modifiedPrecursorVariations = findPrecursorVariations(tempSignificantMassDeltaExplanationsMap);
        LOGGER.debug("finished finding precursor variations");
        //For each of these 'variations' we then calculate all possible fragment ions.

        ///////////////////////////////////////////////////////////////////////
        //FOURTH STEP:  create theoretical fragment ions for all precursors
        //               match them onto the peaks in the spectrum and decide
        //               which one is the best 'explanation'
        LOGGER.info("finding best matches");
        modifiedPrecursors.addAll(findBestMatches(modifiedPrecursorVariations));
        LOGGER.debug("finished finding best matches");
        //remove all explained in this step
        unexplainedModifications.removeAll(spectrumAnnotatorResult.getUnmodifiedPrecursors());
        unexplainedModifications.removeAll(spectrumAnnotatorResult.getModifiedPrecursors());
        significantMassDeltaExplanationsMap.putAll(tempSignificantMassDeltaExplanationsMap);
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
            LOGGER.error("ERROR! Could not calculate masses for all (complete) precursors!");
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
    private Map<Identification, Set<ModificationCombination>> findModificationCombinations(MassRecalibrationResult massRecalibrationResult, List<Identification> identifications) {
        Map<Identification, Set<ModificationCombination>> possibleExplanations = new HashMap<>();
        //check if the modification holder contains at least one modification
        if (!modificationHolder.getAllModifications().isEmpty()) {
            massDeltaExplainer = new MassDeltaExplainerImpl(modificationHolder);
            //finally calculate the possible explanations
            possibleExplanations = massDeltaExplainer.explainCompleteIndentifications(identifications, massRecalibrationResult, analyzerData);
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
        LOGGER.debug("Peptide variations found for " + precursorVariations.size() + " peptides.");
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
            //     ModifiedPeptidesMatchResult modifiedPeptidesMatchResult = spectrumMatcher.findBestModifiedPeptideMatch(identification.getPeptide(), precursorVariations.get(identification), peaks, analyzerData.getFragmentMassError());
            ModifiedPeptidesMatchResult modifiedPeptidesMatchResult = spectrumMatcher.findBestModifiedPeptideMatch(identification.getPeptide(), precursorVariations.get(identification), peaks, courseFragmentAccuraccy);

            if (modifiedPeptidesMatchResult != null) {
                identification.setPeptide(modifiedPeptidesMatchResult.getModifiedPeptide());
                identification.setAnnotationData(modifiedPeptidesMatchResult.getAnnotationData());
                identification.setPipelineExplanationType(PipelineExplanationType.MODIFIED);
                bestMatches.add(identification);
            } else {
                LOGGER.info("No best match found for precursor: " + identification.getPeptide());
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
                AnnotationData annotationData = spectrumMatcher.matchPrecursor(identification.getPeptide(), spectrumService.getSpectrumPeaksBySpectrumId(identification.getSpectrumId().replace("index=", "")), courseFragmentAccuraccy);
                identification.setAnnotationData(annotationData);
                identification.setPipelineExplanationType(PipelineExplanationType.UNEXPLAINED);
                unexplainedIdentifications.add(identification);
            }
        }
        return unexplainedIdentifications;
    }

    private Set<Modification> convertToUseCase(Set<Modification> modifications) {
        HashSet<Modification> temp = new HashSet<>();
        for (Modification aMod : modifications) {
            //oxidation is too general, must be transformed into another variant (oxidation of m for example)
            if (aMod.getAccession().equalsIgnoreCase("UNIMOD:35")) {
                //replace with the non-generic PRIDE modification 
                aMod = (Modification) PRIDEModificationFactory.getInstance().getModificationFromAccession(new AsapModificationAdapter(), "MOD:00719");
            }
            temp.add(aMod);
        }
        return temp;
    }

}
