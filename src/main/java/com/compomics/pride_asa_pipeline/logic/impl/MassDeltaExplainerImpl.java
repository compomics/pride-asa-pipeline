package com.compomics.pride_asa_pipeline.logic.impl;

import com.compomics.pride_asa_pipeline.config.PropertiesConfigurationHolder;
import com.compomics.pride_asa_pipeline.logic.MassDeltaExplainer;
import com.compomics.pride_asa_pipeline.logic.ModificationCombinationSolver;
import com.compomics.pride_asa_pipeline.model.*;
import java.util.*;
import org.apache.log4j.Logger;

/**
 * @author Florian Reisinger Date: 04-Sep-2009
 * @since 0.1
 */
public class MassDeltaExplainerImpl implements MassDeltaExplainer {

    private static final Logger LOGGER = Logger.getLogger(MassDeltaExplainerImpl.class);
    
    private double convergenceCriterion;
    private ModificationCombinationSolver modificationCombinationSolver;
    private MassRecalibrationResult massRecalibrationResult;
    private AnalyzerData analyzerData;

    public MassDeltaExplainerImpl() {
        //get default value for convergence criterion
        convergenceCriterion = PropertiesConfigurationHolder.getInstance().getDouble("massdeltaexplainer.mass_delta_convergence_criterion");
    }

    public double getConvergenceCriterion() {
        return convergenceCriterion;
    }

    public void setConvergenceCriterion(double convergenceCriterion) {
        this.convergenceCriterion = convergenceCriterion;
    }

    @Override
    public ModificationCombinationSolver getModificationCombinationSolver() {
        return modificationCombinationSolver;
    }

    public void setModificationCombinationSolver(ModificationCombinationSolver modificationCombinationSolver) {
        this.modificationCombinationSolver = modificationCombinationSolver;
    }

    public MassRecalibrationResult getMassRecalibrationResult() {
        return massRecalibrationResult;
    }

    @Override
    public void setMassRecalibrationResult(MassRecalibrationResult massRecalibrationResult) {
        this.massRecalibrationResult = massRecalibrationResult;
    }

    public AnalyzerData getAnalyzerData() {
        return analyzerData;
    }

    @Override
    public void setAnalyzerData(AnalyzerData analyzerData) {
        this.analyzerData = analyzerData;
    }

    @Override
    public Map<Identification, Set<ModificationCombination>> explainCompleteIndentifications(List<Identification> identifications) {

        Map<Identification, Set<ModificationCombination>> possibleExplainedIdentifications = new HashMap<Identification, Set<ModificationCombination>>();

        //keep track of ratios determining the loop condition
        //initialized negative to the ratio difference will be positive!
        double previousExplainedRatio = -1D;
        //initially we don't have a ratio
        double currentExplainedRatio = 0D;

        //start with a bag size (number of considered modifications) of 1 and increment
        //until convergence (ratio does not change more than given convergenceCriterion)
        int modificationCombinationSizeLimit = 1;

        //and loop until convergence (ratio does no longer change more than the conversionCriterion)
        while ((currentExplainedRatio - previousExplainedRatio) >= convergenceCriterion) {
            //try to explain all identifications
            for (Identification identification : identifications) {
                Peptide peptide = identification.getPeptide();

                //get the (reported) peptide mass delta
                double precursorMassDelta = 0.0;
                try {
                    precursorMassDelta = peptide.calculateMassDelta();
                } catch (AASequenceMassUnknownException e) {
                    //should not happen, since we are now working with completely known precursors!
                    //ToDo: maybe throw checked exception as we expect 'known' precursors, but could get others
                    throw new IllegalStateException("Unknown peptide mass in 'known' presursor!!");
                }

                //if we have a mass adjustment from previously calculated mass recalibration results
                //we apply the adjustment for the charge state of the current peptide
                if (massRecalibrationResult != null) {
                    //work with adjusted masses
                    Double errorAdjustment = massRecalibrationResult.getError(peptide.getCharge());
                    if (errorAdjustment != null) {
                        precursorMassDelta += errorAdjustment;
                    }
                }

                //if adjustment, use error window,
                //else use default or reported value for analyzer                

                //get default deviation
                Double deviation = PropertiesConfigurationHolder.getInstance().getDouble("massdeltaexplainer.default_deviation");
                if (massRecalibrationResult == null) {
                    //if mass recalibration result is null, use precursor mass error from analyzer data
                    if (analyzerData != null) {
                        if (analyzerData.getPrecursorMassError() != null) {
                            deviation = analyzerData.getPrecursorMassError();
                        }
                    }
                } else {
                    //ToDo: is it correct to use this value here?
                    Double errorWindow = massRecalibrationResult.getErrorWindow(peptide.getCharge());
                    if (errorWindow != null) {
                        deviation = errorWindow;
                    }
                }

                Set<ModificationCombination> combinations = null;
                //if the mass delta is larger than a possible mass error (deviation) then
                //we might have modifications we need to explain (or at least try to)
                if (precursorMassDelta > deviation) {                    
                    combinations = modificationCombinationSolver.findModificationCombinations(peptide, modificationCombinationSizeLimit, precursorMassDelta, deviation);

                    //add all newly found combinations (if any) to the set of all modifications for this peptide
                    if (combinations != null && combinations.size() > 0) {
                        if (possibleExplainedIdentifications.get(identification) == null) {
                            possibleExplainedIdentifications.put(identification, new HashSet<ModificationCombination>());
                        }
                        possibleExplainedIdentifications.get(identification).addAll(combinations);
                    } else {
                        //there is a mass delta the needs to be explained, but
                        //we could not find a suitable modification combination
                        //so we don't add it to the map!
                    }
                } else {
                    //if the mass delta is smaller than a expected mass error,
                    //there is no point in trying to fit modifications
                    //ToDo: maybe add to separate list? for easier reporting?
                    possibleExplainedIdentifications.put(identification, null);
                }

            }
            //store the last ratio
            previousExplainedRatio = currentExplainedRatio;
            //calculate the new ratio of explained precursors
            currentExplainedRatio = (double) possibleExplainedIdentifications.keySet().size() / (double) identifications.size();
            LOGGER.debug("Explanation ratio with " + modificationCombinationSizeLimit + " modifications: " + currentExplainedRatio);

            //increase the number of considered modifications for the next round in case we are not reaching the convergence
            modificationCombinationSizeLimit++;
        }

        //finally return the map of precursors with their possible explantions
        return possibleExplainedIdentifications;
    }
}
