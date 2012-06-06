/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pride_asa_pipeline.model;

import java.util.List;
import java.util.Map;

/**
 * Convenience class for holding the pipeline results
 *
 * @author Niels Hulstaert
 */
public class SpectrumAnnotatorResult {
    
    /**
     * The mass recalibration result
     */
    private MassRecalibrationResult massRecalibrationResult;
    /**
     * The unexplainable identifications
     */
    private List<Identification> unexplainableIdentifications;
    /**
     * The unmodified identifications
     */
    private List<Identification> unmodifiedPrecursors;
    /**
     * The modified identifications
     */
    private Map<Identification, ModifiedPeptide> modifiedPrecursors;

    public MassRecalibrationResult getMassRecalibrationResult() {
        return massRecalibrationResult;
    }

    public void setMassRecalibrationResult(MassRecalibrationResult massRecalibrationResult) {
        this.massRecalibrationResult = massRecalibrationResult;
    }

    public Map<Identification, ModifiedPeptide> getModifiedPrecursors() {
        return modifiedPrecursors;
    }

    public void setModifiedPrecursors(Map<Identification, ModifiedPeptide> modifiedPrecursors) {
        this.modifiedPrecursors = modifiedPrecursors;
    }

    public List<Identification> getUnexplainableIdentifications() {
        return unexplainableIdentifications;
    }

    public void setUnexplainableIdentifications(List<Identification> unexplainableIdentifications) {
        this.unexplainableIdentifications = unexplainableIdentifications;
    }

    public List<Identification> getUnmodifiedPrecursors() {
        return unmodifiedPrecursors;
    }

    public void setUnmodifiedPrecursors(List<Identification> unmodifiedPrecursors) {
        this.unmodifiedPrecursors = unmodifiedPrecursors;
    }
        
}
