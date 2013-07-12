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
    private ModificationCombinationSolver modificationCombinationSolver;

    public MassDeltaExplainerImpl(ModificationHolder modificationHolder) {
        System.out.println("----------------------- new MassDeltaExplainerImpl instance created by thread " + Thread.currentThread().getName());
        modificationCombinationSolver = new ModificationCombinationSolverImpl(modificationHolder);
    }

    public double getConvergenceCriterion() {
        return PropertiesConfigurationHolder.getInstance().getDouble("massdeltaexplainer.mass_delta_convergence_criterion");
    }
    
    @Override
    public Map<Identification, Set<ModificationCombination>> explainCompleteIndentifications(List<Identification> identifications, MassRecalibrationResult massRecalibrationResult, AnalyzerData analyzerData) {
        Map<Identification, Set<ModificationCombination>> possibleExplainedIdentifications = new HashMap<>();

        //keep track of ratios determining the loop condition
        //initialized negative to the ratio difference will be positive!
        double previousExplainedRatio = -1D;
        //initially we don't have a ratio
        double currentExplainedRatio = 0D;

        //start with a bag size (number of considered modifications) of 1 and increment
        //until convergence (ratio does not change more than given convergenceCriterion)
        int modificationCombinationSizeLimit = 1;

        //and loop until convergence (ratio does no longer change more than the conversionCriterion)
        while ((currentExplainedRatio - previousExplainedRatio) >= PropertiesConfigurationHolder.getInstance().getDouble("massdeltaexplainer.mass_delta_convergence_criterion")) {
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
                //work with adjusted masses
                Double errorAdjustment = massRecalibrationResult.getError(peptide.getCharge());
                //check if errorAdjustment is not null and is not equal to zero
                if (errorAdjustment != null && (Math.abs(0.0 - errorAdjustment) > 0.00001)) {
                    precursorMassDelta -= errorAdjustment;
                }

                //take the error window from the mass recalibration as deviation
                //ToDo: is it correct to use this value here?
                Double deviation = massRecalibrationResult.getErrorWindow(peptide.getCharge());
                //check for unconsidered charge states and take the default systematic mass error
                if(deviation == null){
                    double errorTolerance = (analyzerData == null) ? PropertiesConfigurationHolder.getInstance().getDouble("massrecalibrator.default_error_tolerance") : analyzerData.getPrecursorMassError();
                    deviation = errorTolerance / peptide.getCharge();
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
