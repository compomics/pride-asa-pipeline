/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pride_asa_pipeline.logic;

import com.compomics.pride_asa_pipeline.model.AnalyzerData;
import com.compomics.pride_asa_pipeline.model.Identification;
import com.compomics.pride_asa_pipeline.model.MassRecalibrationResult;
import com.compomics.pride_asa_pipeline.model.ModificationCombination;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author niels
 */
public interface MassDeltaExplainer {
    
    /**
     * Explains the given list of complete identifications with the 
     * 
     * @param completeIdentifications the complete identifications
     * @return the 
     */
    Map<Identification, Set<ModificationCombination>> explainCompleteIndentifications(List<Identification> completeIdentifications);
    
    /**
     * Sets the mass recalibration result
     * 
     * @param massRecalibrationResult the mass recalibration result
     */
    void setMassRecalibrationResult(MassRecalibrationResult massRecalibrationResult);
    
    /**
     * Gets the ModificationResolver.
     * 
     * @return the ModificationResolver.
     */
    ModificationCombinationSolver getModificationCombinationSolver();
    
    /**
     * Sets the analyzer data 
     * 
     * @param analyzerData the analyzer data 
     */
    void setAnalyzerData(AnalyzerData analyzerData);
    
}
