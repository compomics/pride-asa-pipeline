/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pride_asa_pipeline.model;

import com.compomics.pride_asa_pipeline.gui.wrapper.IdentificationGuiWrapper;
import java.util.ArrayList;
import java.util.List;

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
     * The unexplained identifications
     */
    private List<Identification> unexplainedIdentifications;
    /**
     * The unmodified identifications
     */
    private List<Identification> unmodifiedPrecursors;
    /**
     * The modified identifications
     */
    private List<Identification> modifiedPrecursors;

    public SpectrumAnnotatorResult() {
        unexplainedIdentifications = new ArrayList<Identification>();
        unmodifiedPrecursors = new ArrayList<Identification>();
        modifiedPrecursors = new ArrayList<Identification>();
    }

    public MassRecalibrationResult getMassRecalibrationResult() {
        return massRecalibrationResult;
    }

    public void setMassRecalibrationResult(MassRecalibrationResult massRecalibrationResult) {
        this.massRecalibrationResult = massRecalibrationResult;
    }

    public List<Identification> getModifiedPrecursors() {
        return modifiedPrecursors;
    }

    public void setModifiedPrecursors(List<Identification> modifiedPrecursors) {
        this.modifiedPrecursors = modifiedPrecursors;
    }

    public List<Identification> getUnexplainedIdentifications() {
        return unexplainedIdentifications;
    }

    public void setUnexplainedIdentifications(List<Identification> unexplainedIdentifications) {
        this.unexplainedIdentifications = unexplainedIdentifications;
    }

    public List<Identification> getUnmodifiedPrecursors() {
        return unmodifiedPrecursors;
    }

    public void setUnmodifiedPrecursors(List<Identification> unmodifiedPrecursors) {
        this.unmodifiedPrecursors = unmodifiedPrecursors;
    }

    /**
     * Returns all the experiment identications.
     *
     * @return the list of experiment identifications
     */
    public List<Identification> getIdentifications() {
        List<Identification> identifications = new ArrayList<Identification>();
        identifications.addAll(unmodifiedPrecursors);
        identifications.addAll(modifiedPrecursors);
        identifications.addAll(unexplainedIdentifications);

        return identifications;
    }

    /**
     * Returns all the experiment Identications as IdentificationGuiWrappers for
     * GUI purposes.
     *
     * @return the list of experiment identification wrappers
     */
    public List<IdentificationGuiWrapper> getIdentificationGuiWrappers() {
        List<IdentificationGuiWrapper> identificationGuiWrappers = new ArrayList<IdentificationGuiWrapper>();
        for (Identification identification : unexplainedIdentifications) {
            identificationGuiWrappers.add(new IdentificationGuiWrapper(identification, IdentificationGuiWrapper.ExplanationType.UNEXPLAINED));
        }
        for (Identification identification : unmodifiedPrecursors) {
            identificationGuiWrappers.add(new IdentificationGuiWrapper(identification, IdentificationGuiWrapper.ExplanationType.UNMODIFIED));
        }
        for (Identification identification : modifiedPrecursors) {
            identificationGuiWrappers.add(new IdentificationGuiWrapper(identification, IdentificationGuiWrapper.ExplanationType.MODIFIED));
        }

        return identificationGuiWrappers;
    }
    
}
