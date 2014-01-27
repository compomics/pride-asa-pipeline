/*
 *

 */
package com.compomics.pride_asa_pipeline.core.logic;

import com.compomics.pride_asa_pipeline.model.AnalyzerData;
import com.compomics.pride_asa_pipeline.model.Identification;
import com.compomics.pride_asa_pipeline.core.model.MassRecalibrationResult;
import com.compomics.pride_asa_pipeline.core.model.ModificationCombination;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Niels Hulstaert
 */
public interface MassDeltaExplainer {

    /**
     * Explains the given list of complete identifications. Returns a map (key:
     * identification, value: the set of modification combinations that can
     * explain the mass delta)
     *
     * @param completeIdentifications the complete identifications
     * @param massRecalibrationResult the mass recalibration result
     * @param analyzerData the analyzer data
     * @return
     */
    Map<Identification, Set<ModificationCombination>> explainCompleteIndentifications(List<Identification> completeIdentifications, MassRecalibrationResult massRecalibrationResult, AnalyzerData analyzerData);    
}
