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

import com.compomics.pride_asa_pipeline.core.cache.ParserCache;
import com.compomics.pride_asa_pipeline.core.data.user.UserSuggestedModifications;
import com.compomics.pride_asa_pipeline.core.logic.spectrum.annotation.AbstractSpectrumAnnotator;
import com.compomics.pride_asa_pipeline.core.logic.inference.IdentificationFilter;
import com.compomics.pride_asa_pipeline.core.logic.modification.InputType;
import com.compomics.pride_asa_pipeline.core.model.MassRecalibrationResult;
import com.compomics.pride_asa_pipeline.core.model.ModificationCombination;
import com.compomics.pride_asa_pipeline.core.model.ModificationHolder;
import com.compomics.pride_asa_pipeline.core.model.SpectrumAnnotatorResult;
import com.compomics.pride_asa_pipeline.core.model.modification.impl.AsapModificationAdapter;
import com.compomics.pride_asa_pipeline.core.model.modification.source.PRIDEModificationFactory;
import com.compomics.pride_asa_pipeline.core.model.instrumentation.ControlledVocabulary;
import com.compomics.pride_asa_pipeline.core.model.instrumentation.MassSpecInstrumentation;
import com.compomics.pride_asa_pipeline.core.service.ExperimentService;
import com.compomics.pride_asa_pipeline.model.AASequenceMassUnknownException;
import com.compomics.pride_asa_pipeline.model.AnalyzerData;
import com.compomics.pride_asa_pipeline.model.AnnotationData;
import com.compomics.pride_asa_pipeline.model.FragmentIonAnnotation;
import com.compomics.pride_asa_pipeline.model.FragmentIonAnnotation.IonType;
import com.compomics.pride_asa_pipeline.model.Identification;
import com.compomics.pride_asa_pipeline.model.Modification;
import com.compomics.pride_asa_pipeline.model.ModifiedPeptide;
import com.compomics.pride_asa_pipeline.model.PipelineExplanationType;
import com.compomics.util.experiment.biology.Ion;
import com.compomics.util.experiment.biology.IonFactory;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import com.compomics.pride_asa_pipeline.core.gui.PipelineProgressMonitor;
import org.springframework.core.io.Resource;
import uk.ac.ebi.pride.utilities.data.controller.impl.ControllerImpl.CachedDataAccessController;
import uk.ac.ebi.pride.utilities.data.core.CvParam;
import uk.ac.ebi.pride.utilities.data.core.ParamGroup;
import uk.ac.ebi.pride.utilities.data.core.Precursor;
import uk.ac.ebi.pride.utilities.data.core.Spectrum;

/**
 *
 * @author Kenneth Verheggen
 */
public class SpectrumAnnotatorImpl extends AbstractSpectrumAnnotator {

    /**
     * The modification adapter to return pride asap modificaitons
     */
    private final AsapModificationAdapter adapter = new AsapModificationAdapter();
    /**
     * The UniMod modification factory for all modifications used in PRIDE
     */
    private final PRIDEModificationFactory modFactory = PRIDEModificationFactory.getInstance();

    private List<Identification> unexplainedBuffer;

    private int sampleSize = 1000;
    private double identificationQualityThreshold = 0.9f;
    private String currentExperiment;
    private String source;
    private String detector;
    private String massSpectrometer;
    private String sourceName;
    private CachedDataAccessController parser;
    private final MassSpecInstrumentation instrumentation = new MassSpecInstrumentation();
    private boolean inMemory = true;
    /**
     * Beans
     */
    private ExperimentService experimentService;

    /**
     * Getters and setters.
     *
     * @return the used experiment service
     */
    public ExperimentService getExperimentService() {
        return experimentService;
    }

    public void setExperimentService(ExperimentService experimentService) {
        this.experimentService = experimentService;
    }

    public MassSpecInstrumentation getInstrumentation() {
        return instrumentation;
    }

    @Override
    public void initIdentifications(String experimentAccession) {
        currentExperiment = experimentAccession;
        PipelineProgressMonitor.info("USING " + SpectrumAnnotatorImpl.class);
        areModificationsLoaded = false;
        parser = ParserCache.getInstance().getParser(currentExperiment, inMemory);
        PipelineProgressMonitor.debug("Creating new SpectrumAnnotatorResult for experiment " + experimentAccession);
        spectrumAnnotatorResult = new SpectrumAnnotatorResult(experimentAccession);
        PipelineProgressMonitor.debug("Loading instrument data from experiment...");
        preloadInstrument();
        PipelineProgressMonitor.debug("Loading charge states for experiment " + experimentAccession);
        initChargeStates();
        PipelineProgressMonitor.info("loading identifications for experiment " + experimentAccession);
        loadExperimentIdentifications(experimentAccession);
        PipelineProgressMonitor.debug("Finished loading identifications for experiment " + experimentAccession);
        ///////////////////////////////////////////////////////////////////////
        //FIRST STEP: find the systematic mass error (if there is one)
        //get analyzer data
        PipelineProgressMonitor.info("finding systematic mass errors");
        MassRecalibrationResult massRecalibrationResult = findSystematicMassError(identifications.getCompletePeptides());
        PipelineProgressMonitor.debug("Finished finding systematic mass errors:" + "\n" + massRecalibrationResult.toString());
        spectrumAnnotatorResult.setMassRecalibrationResult(massRecalibrationResult);
    }

    private void preloadInstrument() {
        source = parser.getMzGraphMetaData().getInstrumentConfigurations().get(0).getSource().get(0).getCvParams().get(0).getAccession();
        sourceName = parser.getMzGraphMetaData().getInstrumentConfigurations().get(0).getSource().get(0).getCvParams().get(0).getName();
        detector = parser.getMzGraphMetaData().getInstrumentConfigurations().get(0).getDetector().get(0).getCvParams().get(0).getName();
        massSpectrometer = parser.getMzGraphMetaData().getInstrumentConfigurations().get(0).getId();
        AnalyzerData analyzerDataByAnalyzerType = AnalyzerData.getAnalyzerDataByAnalyzerType(massSpectrometer);
        //check the initial accuraccy as being the full mass error?
        instrumentation.setData(AnalyzerData.getDataDerivedAnalyzerDataByAnalyzerType(
                analyzerDataByAnalyzerType.getPrecursorMassError(),
                analyzerDataByAnalyzerType.getFragmentMassError(),
                massSpectrometer));

        inferCharges(source);
        PipelineProgressMonitor.info("Detected instrument : " + massSpectrometer
                + "-" + sourceName
                + "-" + detector
                + " [" + instrumentation.getPossibleCharges().first().toString() + " to " + instrumentation.getPossibleCharges().last().toString() + "]");
    }

    @Override
    protected void initChargeStates() {
        //load default values for considered charge states
        consideredChargeStates = instrumentation.getPossibleCharges();
    }

    @Override
    public Set<Modification> initModifications(Resource modificationsResource, InputType inputType) {
        if (!areModificationsLoaded) {
            areModificationsLoaded = true;
            modificationHolder = new ModificationHolder();
            int processed = 0;
            for (Comparable proteinID : parser.getProteinIds()) {
                for (Comparable peptideID : parser.getPeptideIds(proteinID)) {
                    processed++;
                    List<uk.ac.ebi.pride.utilities.data.core.Modification> mods = parser.getPTMs(proteinID, peptideID);
                    for (uk.ac.ebi.pride.utilities.data.core.Modification aMod : mods) {
                        modificationHolder.addModification((Modification) modFactory.getModification(adapter, aMod.getName()));
                    }
                    if (sampleSize > 0 && processed >= sampleSize) {
                        break;
                    }
                }
            }

            //also include user suggested modifications...
            Set<Modification> additionalModifications = UserSuggestedModifications.getInstance().getAdditionalModifications();
            if (!additionalModifications.isEmpty()) {
                PipelineProgressMonitor.info("Loaded user specified modifications :");
                for (Modification mod : additionalModifications) {
                    modificationHolder.addModification(mod);
                    PipelineProgressMonitor.info(mod.getName());
                }
            }
        }
        return modificationHolder.getAllModifications();
    }

    /**
     * Method that handles the annotation of the data en infers a significant
     * mass delta map
     *
     * @param prideModifications Additional modifications that need to be
     * searched
     * @param modifiedPrecursors A list of modified precursor topIdentifications
     * @param unmodifiedPrecursors A list of unmodified precursor
     * topIdentifications
     * @param unexplainedBuffer
     * @param unexplainedIdentifications
     * @param significantMassDeltaExplanationsMap
     */
    @Override
    protected void annotateModifications(
            Set<Modification> prideModifications,
            List<Identification> modifiedPrecursors,
            List<Identification> unmodifiedPrecursors,
            List<Identification> unexplainedBuffer,
            List<Identification> unexplainedIdentifications,
            Map<Identification, Set<ModificationCombination>> significantMassDeltaExplanationsMap) {
        modificationHolder = new ModificationHolder();
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
        PipelineProgressMonitor.processInfo("Inferring modifications");
        //set fragment mass error for the identification scorer
        Map<Identification, Set<ModificationCombination>> massDeltaExplanationsMap = findModificationCombinations(spectrumAnnotatorResult.getMassRecalibrationResult(), identifications);
        PipelineProgressMonitor.debug("Finished finding modification combinations");

        //the returned possibleExplanations map will contain all precursors for which a
        //possible explanation was found or which do not need to be explained (e.g. the
        //mass delta is smaller than the expected mass error)
        //-> the only precursors not in this map are those that carry a significant
        //modification, but nevertheless could not be explained!
        int explainedIdentificationsSize = massDeltaExplanationsMap.size();
        int completeIdentificationsSize = unexplainedBuffer.size();
        PipelineProgressMonitor.debug("Precursors for which no modification combination could be found: " + (completeIdentificationsSize - explainedIdentificationsSize));
        unexplainedIdentifications = getUnexplainedIdentifications(unexplainedBuffer, massDeltaExplanationsMap.keySet());
        for (Identification identification : unexplainedIdentifications) {
            try {
                PipelineProgressMonitor.debug("Unresolved precursor: " + identification.getPeptide().toString() + " with mass delta: " + identification.getPeptide().calculateMassDelta());
            } catch (AASequenceMassUnknownException e) {
                PipelineProgressMonitor.error(e.getMessage(), e);
            }
        }

        for (Identification identification : massDeltaExplanationsMap.keySet()) {
            if (massDeltaExplanationsMap.get(identification) != null) {
                significantMassDeltaExplanationsMap.put(identification, massDeltaExplanationsMap.get(identification));
            } else {
                identification.setPipelineExplanationType(PipelineExplanationType.UNMODIFIED);
                unmodifiedPrecursors.add(identification);

                //annotate the unmodified topIdentifications
                //score the unmodified identification
                //    AnnotationData annotationData = spectrumMatcher.matchPrecursor(identification.getPeptide(), spectrumService.getSpectrumPeaksBySpectrumId(identification.getSpectrumId()), analyzerData.getFragmentMassError());
                AnnotationData annotationData = spectrumMatcher.matchPrecursor(identification.getPeptide(), spectrumService.getSpectrumPeaksBySpectrumId(identification.getSpectrumId()), 1.0);
                identification.setAnnotationData(annotationData);
            }
        }

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
        modifiedPrecursors.addAll(findBestMatches(modifiedPrecursorVariations));
        PipelineProgressMonitor.info("finished finding best matches");
        //remove all explained in this step
        unexplainedBuffer.removeAll(spectrumAnnotatorResult.getUnmodifiedPrecursors());
        unexplainedBuffer.removeAll(spectrumAnnotatorResult.getModifiedPrecursors());
        this.unexplainedBuffer = unexplainedBuffer;
        inferTolerances();

    }

    private void inferTolerances() {
        //DO POST PROCESSING HERE
        IdentificationFilter filter = new IdentificationFilter(identifications.getCompleteIdentifications());
        List<Identification> rankedIDs = filter.ScanTrustworthyIdentifications(90, 75);
        instrumentation.setPrecAcc(inferPrecursorError(rankedIDs));
        instrumentation.setFragAcc(inferFragmentAccuraccy(rankedIDs));
    }

    /**
     * Clears the pipeline resources
     */
    @Override
    public void clearPipeline() {
        areModificationsLoaded = false;
        consideredChargeStates = null;
        identifications = null;
        spectrumAnnotatorResult = null;
        modificationHolder = null;
    }

    /**
     * Clears the result files generated by the pipeline.
     */
    @Override
    public void clearTmpResources() {

//        String path_tmp = PropertiesConfigurationHolder.getInstance().getString("results_path_tmp");
//        File tempDir = new File(path_tmp);
//        try {
//            PipelineProgressMonitor.debug(String.format("clearing tmp resources from folder '%s'", tempDir.getAbsolutePath()));
//            Files.deleteDirectoryContents(tempDir);
//        } catch (IOException e) {
//            PipelineProgressMonitor.error(e.getMessage(), e);
//        }
    }

    /**
     * Private methods
     */
    /**
     * Loads the experiment topIdentifications.
     *
     * @param experimentAccession the experiment accession number
     */
    private void loadExperimentIdentifications(String experimentAccession) {
        currentExperiment = experimentAccession;
        //load the topIdentifications for the given experiment
        identifications = experimentService.loadExperimentIdentifications(experimentAccession);
        //update the considered charge states (if necessary)
        experimentService.updateChargeStates(experimentAccession, consideredChargeStates);
    }

    private double inferPrecursorError(Collection<Identification> filteredIdentifications) {
        DescriptiveStatistics precursorInferenceStatistics = new DescriptiveStatistics();

        for (Identification anIdentification : filteredIdentifications) {
            //   PipelineProgressMonitor.info(anIdentification.getAnnotationData().getIdentificationScore().getMatchingPeaks() + " - "+anIdentification.getAnnotationData().getIdentificationScore().getTotalPeaks());
            if (anIdentification.getPipelineExplanationType() != PipelineExplanationType.UNEXPLAINED) {
                try {

                    try {
                        double precursor_mass_error = Math.abs(anIdentification.getPeptide().calculateMassDelta());
                        double precursor_theoretical_mass = anIdentification.getPeptide().calculateTheoreticalMass();
                        //to ppm                        
                        double ppm = 1000000 / (precursor_theoretical_mass / precursor_mass_error);
                        //to dalton
                        if (ppm <= 1000.0) {
                            precursorInferenceStatistics.addValue(ppm);
                        }
                    } catch (AASequenceMassUnknownException ex) {
                        PipelineProgressMonitor.warn(anIdentification.getPeptide().getSequenceString() + " contains unknown amino acids and will be skipped");
                    }

                    //          }
                } catch (NullPointerException e) {
                    //this can happen if there are no unmodified and identified peptides?
                    //  PipelineProgressMonitor.error("Not able to extract for " + anIdentification);
                }
            }
        }

        int currentPercentile = 100;
        DecimalFormat format = new DecimalFormat("##.000");
        while (currentPercentile > 0) {
            double spread = Math.abs(precursorInferenceStatistics.getPercentile(currentPercentile)) - precursorInferenceStatistics.getPercentile(currentPercentile - 0.5);
            if (spread < precursorInferenceStatistics.getStandardDeviation()) {
                break;
            }
            currentPercentile -= 0.5;
        }
        return Math.ceil(precursorInferenceStatistics.getPercentile(Math.max(0.001, currentPercentile)));
    }

    private double inferFragmentAccuraccy(Collection<Identification> filteredIdentifications) {
        IonFactory fragmentFactory = IonFactory.getInstance();
        DescriptiveStatistics fragmentErrors = new DescriptiveStatistics();

        for (Identification anExpIdentification : filteredIdentifications) {

            HashMap<IonType, TreeSet<Double>> observedMasses = new HashMap<>();
            HashMap<IonType, TreeSet<Double>> theoreticalMasses = new HashMap<>();
            //Get the observed masses
            //check all ions
            if (anExpIdentification.getAnnotationData() != null && anExpIdentification.getPipelineExplanationType() != PipelineExplanationType.UNEXPLAINED) {
                List<FragmentIonAnnotation> fragmentIonAnnotations = anExpIdentification.getAnnotationData().getFragmentIonAnnotations();
                if (fragmentIonAnnotations != null && !fragmentIonAnnotations.isEmpty()) {
                    for (FragmentIonAnnotation fragmentIon : fragmentIonAnnotations) {
                        double observedMass = fragmentIon.getIon_charge() * fragmentIon.getMz();
                        IonType currentIon = null;
                        switch (fragmentIon.getIon_type_name()) {
                            case "b ion":
                                currentIon = IonType.B_ION;
                                break;
                            case "y ion":
                                currentIon = IonType.Y_ION;
                                break;
                        }
                        if (currentIon != null) {
                            TreeSet<Double> masses = observedMasses.getOrDefault(currentIon, new TreeSet<Double>());
                            masses.add(observedMass);
                            observedMasses.put(currentIon, masses);
                        }
                    }
                }
            }

            //get the theoretical
            com.compomics.util.experiment.biology.Peptide tmp = new com.compomics.util.experiment.biology.Peptide(anExpIdentification.getPeptide().getSequenceString(), null);
            HashMap<Integer, HashMap<Integer, ArrayList<Ion>>> fragmentIons = fragmentFactory.getFragmentIons(tmp);
            for (int ionType : fragmentIons.keySet()) {
                for (int ionSubType : fragmentIons.get(ionType).keySet()) {
                    if (fragmentIons.get(ionSubType) != null) {
                        for (ArrayList<Ion> ionlist : fragmentIons.get(ionSubType).values()) {
                            for (Ion ion : ionlist) {
                                double theoreticMass = ion.getTheoreticMass();
                                IonType currentIon = null;
                                switch (ion.getSubTypeAsString()) {
                                    case "b":
                                        currentIon = IonType.B_ION;
                                        break;
                                    case "y":
                                        currentIon = IonType.Y_ION;
                                        break;
                                }
                                if (currentIon != null) {
                                    TreeSet<Double> masses = theoreticalMasses.getOrDefault(currentIon, new TreeSet<Double>());
                                    masses.add(theoreticMass);
                                    theoreticalMasses.put(currentIon, masses);
                                }
                            }
                        }
                    }
                }
            }

            for (IonType ionType : observedMasses.keySet()) {
                TreeSet<Double> observedMassList = observedMasses.get(ionType);
                TreeSet<Double> theoreticalMassList = theoreticalMasses.get(ionType);
                for (Double observedMass : observedMassList) {
                    double fragmentMassError = -1;
                    for (Double theoreticalMass : theoreticalMassList) {
                        double absDelta = Math.abs(theoreticalMass - observedMass);
                        if (fragmentMassError == -1 || absDelta < fragmentMassError) {
                            fragmentMassError = absDelta;
                        }
                    }
                    if (fragmentMassError > 0 && fragmentMassError < 1.0) {
                        fragmentErrors.addValue(fragmentMassError);
                    }
                }
            }
        }
//PERCENTILE CAN NOT BE 0
        double currentPercentile = 0.001;
        double tmpPercentile;
        while (currentPercentile < 100) {    
            //clamp at 100
            tmpPercentile=Math.min(100,currentPercentile + 0.5);

            double spread = Math.abs(fragmentErrors.getPercentile(currentPercentile)) - fragmentErrors.getPercentile(tmpPercentile);
            if (spread < fragmentErrors.getStandardDeviation()) {
                break;
            }
            currentPercentile += 0.5;
        }
        return (Math.ceil(fragmentErrors.getPercentile(Math.min(100,currentPercentile)) * 100d) / 100d);
    }

    /**
     * Private methods
     */
    /**
     * Infers charges from the annotated source
     *
     * @param experimentAccession the experiment accession number
     */
    private void inferCharges(String source) {
        int[] detectedCharges = findChargeRange();
        //IF MALDI ---> only single charge or even charges possible...
        if (source.contains(ControlledVocabulary.MALDI.getTerm())) {
            //check the possible charges and reduce them to 1 in case they were searched with...
            if (detectedCharges[1] > 1) {
                //we may have an issue here...maldi does not usually produce higher ions so this is a non standard study
                PipelineProgressMonitor.warn("The detected ion source was MALDI and usually does not produce ions above charge state 1+, ignoring higher states");
            }
            instrumentation.getPossibleCharges().add(1);
        } else if (source.contains(ControlledVocabulary.ESI.getTerm())) {

            if (detectedCharges[0] == 1) {
                //we may have an issue here...esi does not usually produce lower ions so this is a non standard study
                PipelineProgressMonitor.warn("The detected ion source was ESI and usually does not produce singly charged ions, ignoring single charge state");
            }
            for (int i = Math.max(2, detectedCharges[0]); i <= detectedCharges[1] + 1; i++) {
                instrumentation.getPossibleCharges().add(i);
            }
        } else {
            for (int i = detectedCharges[0]; i <= detectedCharges[1] + 1; i++) {
                instrumentation.getPossibleCharges().add(i);
            }
        }
    }

    /**
     * Infers the range of potential charges from the spectrum IDs...
     *
     * @return the charge range
     */
    private int[] findChargeRange() {
        TreeSet<Integer> charges = new TreeSet<>();
        for (Comparable spectrumID : parser.getSpectrumIds()) {
            if (charges.size() >= 1000) {
                PipelineProgressMonitor.info("Sampled 1000 spectra");
                break;
            }
            Spectrum spectrumById = parser.getSpectrumById(spectrumID);
            for (Precursor precursor : spectrumById.getPrecursors()) {
                for (ParamGroup group : precursor.getSelectedIons()) {
                    for (CvParam param : group.getCvParams()) {
                        if (ControlledVocabulary.CHARGE_STATE.getTerm().equalsIgnoreCase(param.getAccession())) {
                            charges.add(Integer.parseInt(param.getValue()));
                        }
                    }
                }

            }
        }
        if (charges.isEmpty()) {
            return new int[]{1, 3};
        } else {
            return new int[]{charges.first(), charges.last()};
        }

    }

}
