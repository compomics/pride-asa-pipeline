package com.compomics.pride_asa_pipeline.pipeline;

import com.compomics.pride_asa_pipeline.config.PropertiesConfigurationHolder;
import com.compomics.pride_asa_pipeline.logic.MassDeltaExplainer;
import com.compomics.pride_asa_pipeline.logic.PeptideVariationsGenerator;
import com.compomics.pride_asa_pipeline.model.*;
import com.compomics.pride_asa_pipeline.recalibration.MassRecalibrator;
import com.compomics.pride_asa_pipeline.service.ExperimentService;
import com.compomics.pride_asa_pipeline.service.ModificationService;
import com.compomics.pride_asa_pipeline.service.SpectrumService;
import com.compomics.pride_asa_pipeline.spectrum.match.SpectrumMatcher;
import java.util.*;
import org.apache.log4j.Logger;

/**
 * Created by IntelliJ IDEA. User: niels Date: 9/11/11 Time: 13:22 To change
 * this template use File | Settings | File Templates.
 */
public class PrideSpectrumAnnotator {

    private static final Logger LOGGER = Logger.getLogger(PrideSpectrumAnnotator.class);
    /**
     * The considered charge states
     */
    private Set<Integer> consideredChargeStates;
    /**
     * The experiment modifications
     */
    private Identifications identifications;
    /**
     * The analyzer data
     */
    private AnalyzerData analyzerData;
    /**
     * The spectrum annotator result
     */
    private SpectrumAnnotatorResult spectrumAnnotatorResult;
    /**
     * Beans
     */
    private ExperimentService experimentService;
    private ModificationService modificationService;
    private MassRecalibrator massRecalibrator;
    private SpectrumService spectrumService;
    private SpectrumMatcher spectrumMatcher;
    private MassDeltaExplainer massDeltaExplainer;
    private PeptideVariationsGenerator peptideVariationsGenerator;

    /**
     * No-arg constructor
     */
    public PrideSpectrumAnnotator() {
        spectrumAnnotatorResult = new SpectrumAnnotatorResult();
        init();
    }

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

    public Set<Integer> getConsideredChargeStates() {
        return consideredChargeStates;
    }

    public void setConsideredChargeStates(Set<Integer> consideredChargeStates) {
        this.consideredChargeStates = consideredChargeStates;
    }

    public MassDeltaExplainer getMassDeltaExplainer() {
        return massDeltaExplainer;
    }

    public void setMassDeltaExplainer(MassDeltaExplainer massDeltaExplainer) {
        this.massDeltaExplainer = massDeltaExplainer;
    }

    public ExperimentService getExperimentService() {
        return experimentService;
    }

    public void setExperimentService(ExperimentService experimentService) {
        this.experimentService = experimentService;
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

    public void setIdentifications(Identifications identifications) {
        this.identifications = identifications;
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

    public SpectrumAnnotatorResult getSpectrumAnnotatorResult() {
        return spectrumAnnotatorResult;
    }

    public void setSpectrumAnnotatorResult(SpectrumAnnotatorResult spectrumAnnotatorResult) {
        this.spectrumAnnotatorResult = spectrumAnnotatorResult;
    }        

    /**
     * Public methods.
     */
    public void loadExperimentIdentifications(String experimentAccession) {
        //load the identifications for the given experiment
        identifications = experimentService.loadExperimentIdentifications(experimentAccession);
        //update the considered charge states (if necessary)
        experimentService.updateChargeStates(experimentAccession, consideredChargeStates);
    }

    public MassRecalibrationResult findSystematicMassError(Set<Integer> consideredChargeStates, List<Peptide> completePeptides) {
        //set considered charge states
        massRecalibrator.setConsideredChargeStates(consideredChargeStates);

        MassRecalibrationResult massRecalibrationResult = null;
        try {
            massRecalibrationResult = massRecalibrator.recalibrate(completePeptides);
        } catch (AASequenceMassUnknownException e) {
            //this should not happen here, since we only handle 'complete precursors' where
            //all the amino acids have a known mass
            LOGGER.error("ERROR! Could not calculate masses for all (complete) precursors!");
            throw new IllegalStateException("Could not calculate masses for all (complete) precursors!");
        }
        return massRecalibrationResult;
    }

    public Map<Identification, Set<ModificationCombination>> findModificationCombinations(MassRecalibrationResult massRecalibrationResult, Identifications identifications) {
        //For the solver we need a ModificationHolder (contains all considered modifications)
        ModificationHolder modificationHolder = new ModificationHolder();

        //add the pipeline modifications
        modificationHolder.addModifications(modificationService.loadPipelineModifications(PropertiesConfigurationHolder.getInstance().getString("modification.pipeline_modifications_file_name")));

        //add the modifications found in pride for the given experiment
        if(PropertiesConfigurationHolder.getInstance().getBoolean("spectrumannotator.include_pride_modifications")){
            modificationHolder.addModifications(modificationService.loadExperimentModifications(identifications.getCompletePeptides()));
        }

        //set MassDeltaExplainer ModificationHolder, MassRecalibrationResult and AnalyzerData
        massDeltaExplainer.getModificationCombinationSolver().setModificationHolder(modificationHolder);
        if (massRecalibrationResult != null) {
            massDeltaExplainer.setMassRecalibrationResult(massRecalibrationResult);
        }
        if (analyzerData != null) {
            massDeltaExplainer.setAnalyzerData(analyzerData);
        }
        //finally calculate the possible explanations
        Map<Identification, Set<ModificationCombination>> possibleExplanations =
                massDeltaExplainer.explainCompleteIndentifications(identifications.getCompleteIdentifications());

        return possibleExplanations;
    }

    public Map<Identification, Set<ModifiedPeptide>> findPrecursorVariations(Map<Identification, Set<ModificationCombination>> possibleExplanations) {
        //From the theoretical information we already have (e.g. the precursor sequence
        //and all possible modifications) we therefore first create all possible
        //'modified precursors' or 'precursor variations' (e.g. the same peptide sequence,
        //but with different modifications on different locations).
        Map<Identification, Set<ModifiedPeptide>> precursorVariations = new HashMap<Identification, Set<ModifiedPeptide>>();
        //ToDo: change for 'true possibilities'
        for (Identification identificationSet : possibleExplanations.keySet()) {
            Set<ModificationCombination> modifications = possibleExplanations.get(identificationSet);
            Set<ModifiedPeptide> precursorVariationsSet = peptideVariationsGenerator.generateVariations(identificationSet.getPrecursor(), modifications);
            precursorVariations.put(identificationSet, precursorVariationsSet);
            //System.out.println("Modification variations for precursor " + iData.getPrecursor() + " : " + precursorVariations.size());
        }
        LOGGER.debug("Peptide variations found for " + precursorVariations.size() + " peptides.");

        return precursorVariations;
    }

    public Map<Identification, ModifiedPeptide> findBestMatches(String experimentAccession, Map<Identification, Set<ModifiedPeptide>> precursorVariations) {
        //After we now have all the theoretical ions for all possible 'precursor variations',
        //we can try and find them in the real spectrum. This will give an indication as to
        //which 'variation' of the precursor is the most likely one.

        //for each precursor: try all ModifiedPeptide variations
        //for all these variations: map against the spectra and assign a score

        //set fragment mass error for the identification scorer
        spectrumMatcher.getIdentificationScorer().setFragmentMassError(analyzerData.getFragmentMassError());

        Map<Identification, ModifiedPeptide> bestMatches = new HashMap<Identification, ModifiedPeptide>();
        for (Identification identification : precursorVariations.keySet()) {
            //load spectrum
            List<Peak> peaks = spectrumService.getSpectrumPeaksBySpectrumId(identification.getSpectrumId());
            ModifiedPeptide bestMatch = spectrumMatcher.findBestModifiedPeptideMatch(identification.getPrecursor(), precursorVariations.get(identification), peaks);
            if (bestMatch != null) {
                bestMatches.put(identification, bestMatch);
            } else {
                LOGGER.info("No best match found for precursor: " + identification.getPrecursor());
            }
        }

        return bestMatches;
    }

    public void annotate(String experimentAccession) {
        LOGGER.info("Loading identification for experiment " + experimentAccession);
        loadExperimentIdentifications(experimentAccession);
        LOGGER.debug("Finisher loading identification for experiment " + experimentAccession);
        
        ///////////////////////////////////////////////////////////////////////
        //FIRST STEP: find the systematic mass error (if there is one)
        LOGGER.info("Finding systematic mass errors");
        MassRecalibrationResult massRecalibrationResult = findSystematicMassError(consideredChargeStates, identifications.getCompletePeptides());
        LOGGER.debug("Finished finding systematic mass errors:" + "\n" + massRecalibrationResult.toString());
        spectrumAnnotatorResult.setMassRecalibrationResult(massRecalibrationResult);

        ///////////////////////////////////////////////////////////////////////
        //SECOND STEP: find all the modification combinations that could
        //              explain a given mass delta (if there is one) -> Zen Archer
        //get analyzer data
        analyzerData = experimentService.getAnalyzerData(experimentAccession);
        LOGGER.info("Finding modification combinations");
        Map<Identification, Set<ModificationCombination>> massDeltaExplanationsMap = findModificationCombinations(massRecalibrationResult, identifications);
        LOGGER.debug("Finished finding modification combinations");

        //the returned possibleExplanations map will contain all precursors for which a
        //possible explanation was found or which do not need to be explained (e.g. the
        //mass delta is smaller than the expected mass error)
        //-> the only precursors not in this map are those that carry a significant
        //modification, but nevertheless could not be explained!
        int explainableIdentificationsSize = massDeltaExplanationsMap.size();
        int completeIdentificationsSize = identifications.getCompleteIdentifications().size();
        LOGGER.debug("Precursors for which no modification combination could be found: " + (completeIdentificationsSize - explainableIdentificationsSize));
        List<Identification> unexplainableIdentifications = getUnexplainableIdentifications(identifications.getCompleteIdentifications(), massDeltaExplanationsMap.keySet());
        assert (unexplainableIdentifications.size() == (completeIdentificationsSize - explainableIdentificationsSize));
        for (Identification identification : unexplainableIdentifications) {
            try {
                LOGGER.debug("Unresolved precursor: " + identification.getPrecursor().toString() + " with mass delta: " + identification.getPrecursor().calculateMassDelta());
            } catch (AASequenceMassUnknownException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
        spectrumAnnotatorResult.setUnexplainableIdentifications(unexplainableIdentifications);

        //create new map with only the precursors that carry a significant mass delta
        //and were we have possible modification combinations
        List<Identification> unmodifiedPrecursors = new ArrayList<Identification>();
        Map<Identification, Set<ModificationCombination>> significantMassDeltaExplanationsMap = new HashMap<Identification, Set<ModificationCombination>>();
        for (Identification identification : massDeltaExplanationsMap.keySet()) {
            if (massDeltaExplanationsMap.get(identification) != null) {
                significantMassDeltaExplanationsMap.put(identification, massDeltaExplanationsMap.get(identification));
            } else {
                unmodifiedPrecursors.add(identification);
            }
        }
        spectrumAnnotatorResult.setUnmodifiedPrecursors(unmodifiedPrecursors);
        LOGGER.debug("Precursors with possible modification(s): " + significantMassDeltaExplanationsMap.size());
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
        Map<Identification, Set<ModifiedPeptide>> modifiedPrecursorVariations = findPrecursorVariations(significantMassDeltaExplanationsMap);
        LOGGER.debug("finished finding precursor variations");
        //For each of these 'variations' we then calculate all possible fragment ions.

        ///////////////////////////////////////////////////////////////////////
        //FOURTH STEP:  create theoretical fragment ions for all precursors
        //               match them onto the peaks in the spectrum and decide
        //               which one is the best 'explanation' 
        LOGGER.info("finding best matches");
        Map<Identification, ModifiedPeptide> modifiedPrecursors = findBestMatches(experimentAccession, modifiedPrecursorVariations);
        LOGGER.debug("finished finding best matches");
        spectrumAnnotatorResult.setModifiedPrecursors(modifiedPrecursors);
    }
    
    /**
     * Private methods
     */
    private void init() {
        //load default values for considered charge states
        consideredChargeStates = new HashSet<Integer>();
        for (Object chargeState : PropertiesConfigurationHolder.getInstance().getList("pipeline.considered_charge_states")) {
            consideredChargeStates.add(Integer.parseInt(chargeState.toString()));
        }
    }
    
    /**
     * Gets the identifications that where the mass delta could't be explained by combining modifications
     * 
     * @param identifications all the experiment identifications
     * @param explainableIdentifications the explained identifications
     * @return the unexplained identifications
     */
    private List<Identification> getUnexplainableIdentifications(List<Identification> identifications, Set<Identification> explainableIdentifications) {
        List<Identification> unexplainedIdentifications = new ArrayList<Identification>();
        for (Identification identification : identifications) {
            if (!explainableIdentifications.contains(identification)) {
                unexplainedIdentifications.add(identification);
            }
        }
        return unexplainedIdentifications;
    }
}
