package com.compomics.pride_asa_pipeline.core.logic;

import com.compomics.pride_asa_pipeline.core.cache.ParserCache;
import com.compomics.pride_asa_pipeline.core.config.PropertiesConfigurationHolder;
import com.compomics.pride_asa_pipeline.core.exceptions.ParameterExtractionException;
import com.compomics.pride_asa_pipeline.core.logic.impl.MassDeltaExplainerImpl;
import com.compomics.pride_asa_pipeline.core.logic.inference.InferenceStatistics;
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
import com.compomics.pride_asa_pipeline.model.AnalyzerData;
import com.compomics.pride_asa_pipeline.model.AnnotationData;
import com.compomics.pride_asa_pipeline.model.Identification;
import com.compomics.pride_asa_pipeline.model.Identifications;
import com.compomics.pride_asa_pipeline.model.Modification;
import com.compomics.pride_asa_pipeline.model.ModifiedPeptide;
import com.compomics.pride_asa_pipeline.model.Peak;
import com.compomics.pride_asa_pipeline.model.Peptide;
import com.compomics.pride_asa_pipeline.model.PipelineExplanationType;
import com.compomics.util.pride.PrideWebService;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import org.apache.log4j.Logger;
import org.springframework.core.io.Resource;
import uk.ac.ebi.pride.archive.web.service.model.assay.AssayDetail;
import uk.ac.ebi.pride.utilities.data.controller.DataAccessException;

/**
 *
 * @author Kenneth Verheggen
 * @author Niels Hulstaert
 */
public abstract class AbstractSpectrumAnnotator<T> {

    private static final Logger LOGGER = Logger.getLogger(AbstractSpectrumAnnotator.class);

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
    private int MAX_PASS_SIZE = 6;
    private int MAX_PASSES = 6;
    private double explanationCriterion = 0.50;
    private double matchedPeakCriterion = 0.1;
    private double maximumAllowedErrorDa = 1.0;
    private ArrayBlockingQueue<Modification> modQueue;

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
    public abstract Set<Modification> initModifications(Resource modificationsResource, InputType inputType);

    /**
     * Clears the pipeline resources.
     */
    public abstract void clearPipeline();

    /**
     * Clears the temp files generated by the pipeline.
     */
    public abstract void clearTmpResources();

    public void annotate(String assayAccession) throws IOException {
        initModifications(assayAccession, null, null);
        initAnalyzerData(assayAccession);
        List<Identification> completeIdentifications = identifications.getCompleteIdentifications();
        List<Identification> identificationsToProcess = new ArrayList<>(completeIdentifications);
        HashMap<Modification, Double> totalModificationRates = new HashMap<>();
        Set<Modification> nextModificationSet;

        int totalSize = completeIdentifications.size();
        int totalExplained = 0;
        int pass = 1;
        double explanationRatio = 0.0;
        while (((nextModificationSet = getNextModificationSet()) != null) && pass <= MAX_PASSES) {
            SpectrumAnnotatorResult tempSpectrumAnnotatorResult = new SpectrumAnnotatorResult();
            tempSpectrumAnnotatorResult.setMassRecalibrationResult(spectrumAnnotatorResult.getMassRecalibrationResult());
            LOGGER.info("Going through " + pass + "th pass.");
            ModificationHolder tempModificationHolder = new ModificationHolder();
            tempModificationHolder.addModifications(nextModificationSet);
            annotate(tempModificationHolder, completeIdentifications, identificationsToProcess, tempSpectrumAnnotatorResult);
            totalExplained = totalSize - identificationsToProcess.size();
            completeIdentifications = identificationsToProcess;
            LOGGER.info("Still need to annotate " + completeIdentifications.size() + " identifications");
            identificationsToProcess = new ArrayList<>(completeIdentifications);
            explanationRatio = (double) totalExplained / totalSize;
            LOGGER.info("Explanationratio is currently " + explanationRatio);
            if (explanationRatio > explanationCriterion) {
                LOGGER.info(100 * explanationCriterion + "% of identifications were identified, moving on");
                break;
            } else {
                Map<Modification, Double> estimateModificationRate = modificationService.estimateModificationRate(modificationService.getUsedModifications(tempSpectrumAnnotatorResult),
                        tempSpectrumAnnotatorResult,
                        0.0);
                for (Map.Entry<Modification, Double> aModificationRate : estimateModificationRate.entrySet()) {
                    totalModificationRates.put(aModificationRate.getKey(), aModificationRate.getValue());
                }
                pass++;
            }
            System.out.println(tempSpectrumAnnotatorResult.getMassRecalibrationResult().toString());
        }
        //filter out only the ones that are most relevant (top 6 or using a consideration threshold)...
        InferenceStatistics stats = new InferenceStatistics(totalModificationRates.values(), false);
        double threshold = Math.max(0.005, stats.getPercentile(2.5));
        //sort the map on values?

        Comparator<Entry<Modification, Double>> byValue = (entry1, entry2) -> entry1.getValue().compareTo(
                entry2.getValue());

        totalModificationRates
                .entrySet()
                .stream()
                .sorted(byValue.reversed()).filter((anEntry) -> (anEntry.getValue() >= threshold)).forEach((anEntry) -> {
            if (modificationHolder.getAllModifications().size() <= 6) {
                LOGGER.info("Considering " + anEntry.getKey() + "\t" + anEntry.getValue());
                modificationHolder.addModification(anEntry.getKey());
            }
        });

    }

    private void consider(Entry<Modification, Double> entry, double threshold) {
        if (entry.getValue() >= threshold) {
            modificationHolder.addModification(entry.getKey());
        }
    }

    public List<Identification> filterIdentifications(List<Identification> completeIdentifications, ModificationHolder modificationHolder) {
        //first remove all identifications that have a mass error smaller than the smallest modification
        double smallestMass = modificationHolder.getAllModifications().iterator().next().getMonoIsotopicMassShift();
        for (Modification aMod : modificationHolder.getAllModifications()) {
            double deltaAbs = Math.abs(aMod.getMonoIsotopicMassShift());
            if (smallestMass > deltaAbs) {
                smallestMass = deltaAbs;
            }
        }
        LOGGER.info("Looking for unexplained masses that are larger than " + smallestMass);
        List<Identification> temp = new ArrayList<>();
        for (Identification ident : completeIdentifications) {
            try {
                if (Math.abs(ident.getPeptide().calculateMassDelta()) < smallestMass) {
                    temp.add(ident);
                }
            } catch (AASequenceMassUnknownException ex) {
                LOGGER.warn(ex);
            }
        }
        //sort identifications from worst to best, more chance to find a mod in a bad match !
        Comparator comparator = new Comparator() {
            @Override
            public int compare(Object o1, Object o2) {
                if (o1 instanceof Identification && o2 instanceof Identification) {
                    Identification ident1 = (Identification) o1;
                    Identification ident2 = (Identification) o2;
                    try {
                        double mr1 = ident1.getPeptide().calculateMassDelta();
                        double mr2 = ident2.getPeptide().calculateMassDelta();
                        if (mr1 > mr2) {
                            return 1;
                        } else if (mr1 < mr2) {
                            return -1;
                        }
                    } catch (AASequenceMassUnknownException e) {
                        LOGGER.warn(e);
                        return -1;
                    }
                }
                return 0;
            }
        };
        Collections.sort(temp, comparator);
        return temp;
    }

    public Set<Modification> initModifications(String assayAccession, Resource modificationsResource, InputType inputType) throws IOException {
        LOGGER.info("Loading modifications...");
        modificationHolder = new ModificationHolder();
        LinkedHashSet<Modification> sortedAnnotatedModifications = new LinkedHashSet<>();
        //load the modifications from the PRIDE annotation
        //if there is no file in the parsercache, use the webservice to get the modifications 
        if (ParserCache.getInstance().containsParser(assayAccession)) {
            FileModificationRepository repository = new FileModificationRepository();
            sortedAnnotatedModifications.addAll(repository.getModificationsByExperimentId(assayAccession));
        } else {
            AnnotatedModificationService annotatedModService = new AnnotatedModificationService();
            AsapModificationAdapter adapter = new AsapModificationAdapter();
            //get other modifications
            for (String aPTMName : annotatedModService.getAssayAnnotatedPTMs(assayAccession)) {
                try {
                    sortedAnnotatedModifications.add((Modification) PRIDEModificationFactory.getInstance().getModification(adapter, aPTMName));
                } catch (ParameterExtractionException ex) {
                    LOGGER.error("Could not include " + aPTMName + ". Please verify ! Reason:" + ex);
                }
            }
        }
        //order the annotated modifications to prevalence (in case there are more than the selected batch size)
        //get all asap mods
        LinkedList<Modification> sortedAllModifications = PRIDEModificationFactory.getAsapMods();
        //get a queue of them
        modQueue = new ArrayBlockingQueue<>(sortedAllModifications.size());
        //first get the annotated modifications and order those as well?
        sortedAnnotatedModifications.stream().map((modObject) -> (Modification) modObject).map((mod) -> {
            modQueue.offer(mod);
            return mod;
        }).forEach((mod) -> {
            sortedAllModifications.remove(mod);
        });
        //add the others
        sortedAllModifications.stream().forEach((mod) -> {
            modQueue.offer(mod);
        });
        LOGGER.info("Retrieved sorted modification map");
        return sortedAnnotatedModifications;
    }

    private Set<Modification> getNextModificationSet() throws IOException {
        //drain the queue in subset parts
        Set<Modification> modPassSet = new HashSet<>();
        //subset the modQueue in x partitions
        if (modQueue.drainTo(modPassSet, MAX_PASS_SIZE) > 0) {
            return modPassSet;
        } else {
            return null;
        }
    }

    /**
     * Annotate the experiment identifications.
     *
     * @param completeIdentifications the list of complete identifications
     * @param unexplainedIdentifications the list of identifications without an
     * explanation (initially this is the same as the complete identifications)
     */
    private void annotate(ModificationHolder modificationHolder, List<Identification> completeIdentifications, List<Identification> identificationsToProcess, SpectrumAnnotatorResult spectrumAnnotatorResult) {
        ///////////////////////////////////////////////////////////////////////
        //SECOND STEP: find all the modification combinations that could
        //              explain a given mass delta (if there is one) -> Zen Archer
        LOGGER.info("finding modification combinations..;");
        //set fragment mass error for the identification scorer

        Map<Identification, Set<ModificationCombination>> massDeltaExplanationsMap = findModificationCombinations(modificationHolder, spectrumAnnotatorResult.getMassRecalibrationResult(), completeIdentifications);
        LOGGER.info("Finished finding modification combinations");

        //the returned possibleExplanations map will contain all precursors for which a
        //possible explanation was found or which do not need to be explained (e.g. the
        //mass delta is smaller than the expected mass error)
        //-> the only precursors not in this map are those that carry a significant
        //modification, but nevertheless could not be explained!
        int explainedIdentificationsSize = massDeltaExplanationsMap.size();
        int completeIdentificationsSize = completeIdentifications.size();
        LOGGER.info("Precursors for which no modification combination could be found: " + (completeIdentificationsSize - explainedIdentificationsSize));
        List<Identification> unexplainedIdentifications = getUnexplainedIdentifications(completeIdentifications, massDeltaExplanationsMap.keySet());
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
                //remove the identification from the list that needs to be processed
                identificationsToProcess.remove(identification);
            }
        }
        spectrumAnnotatorResult.setUnmodifiedPrecursors(unmodifiedPrecursors);
        LOGGER.info("Precursors with possible modification(s): " + significantMassDeltaExplanationsMap.size());
        LOGGER.info("Precursors with mass delta smaller than mass error (probably unmodified): " + unmodifiedPrecursors.size());

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
        LOGGER.info("finished finding precursor variations");
        //For each of these 'variations' we then calculate all possible fragment ions.

        ///////////////////////////////////////////////////////////////////////
        //FOURTH STEP:  create theoretical fragment ions for all precursors
        //               match them onto the peaks in the spectrum and decide
        //               which one is the best 'explanation'
        LOGGER.info("finding best matches");
        List<Identification> modifiedPrecursors = findBestMatches(modifiedPrecursorVariations);
        LOGGER.info("finished finding best matches");
        spectrumAnnotatorResult.setModifiedPrecursors(modifiedPrecursors);
        //remove all these from the list to process
        identificationsToProcess.removeAll(modifiedPrecursors);
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
    private Map<Identification, Set<ModificationCombination>> findModificationCombinations(ModificationHolder modificationHolder, MassRecalibrationResult massRecalibrationResult, List<Identification> completeIdentifications) {
        Map<Identification, Set<ModificationCombination>> possibleExplanations = new HashMap<>();

        //check if the modification holder contains at least one modification
        if (!modificationHolder.getAllModifications().isEmpty()) {
            massDeltaExplainer = new MassDeltaExplainerImpl(modificationHolder);
            //finally calculate the possible explanations
            //prefilter the identifications here...if it's smaller than the smallest mod, there's no point in keeping it
            List<Identification> filterIdentifications = filterIdentifications(completeIdentifications, modificationHolder);
            possibleExplanations = massDeltaExplainer.explainCompleteIndentifications(filterIdentifications, massRecalibrationResult, analyzerData);
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
        for (Identification identification : possibleExplanations.keySet()) {
            Set<ModificationCombination> modifications = possibleExplanations.get(identification);
            Set<ModifiedPeptide> precursorVariationsSet = peptideVariationsGenerator.generateVariations(identification.getPeptide(), modifications);
            precursorVariations.put(identification, precursorVariationsSet);
        }
        LOGGER.info("Peptide variations found for " + precursorVariations.size() + " peptides.");

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
                //annotate the unexplained identifications
                //score the unexplained identification
                try {
                    AnnotationData annotationData = spectrumMatcher.matchPrecursor(identification.getPeptide(), spectrumService.getSpectrumPeaksBySpectrumId(identification.getSpectrumId().replace("index=", "")), analyzerData.getFragmentMassError());
                    identification.setAnnotationData(annotationData);
                    identification.setPipelineExplanationType(PipelineExplanationType.UNEXPLAINED);
                    unexplainedIdentifications.add(identification);
                } catch (NullPointerException | DataAccessException e) {
                    LOGGER.error("Something went wrong for " + identification.getPeptide().getSequenceString() + ":", e);
                }
            }
        }
        return unexplainedIdentifications;
    }

    private void initAnalyzerData(String assay) throws IOException {
        if (analyzerData == null) {

            try {
                AssayDetail assayDetail = PrideWebService.getAssayDetail(assay);
                Set<String> instrumentNames = assayDetail.getInstrumentNames();
                if (instrumentNames.size() > 0) {
                    LOGGER.warn("There are multiple instruments, selecting lowest precursor accuraccy...");
                }
                for (String anInstrumentName : instrumentNames) {
                    AnalyzerData temp = AnalyzerData.getAnalyzerDataByAnalyzerType(anInstrumentName);
                    //worst precursor has benefit
                    if (analyzerData.getPrecursorAccuraccy() > temp.getPrecursorAccuraccy()) {
                        analyzerData = temp;
                    }
                }
            } catch (Exception e) {
                LOGGER.warn("Could not retrieve analyzer data from pride webservice.");
                analyzerData = AnalyzerData.getAnalyzerDataByAnalyzerType("");
            }
        }
    }
}
