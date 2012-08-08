/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pride_asa_pipeline.gui.wrapper;

import com.compomics.pride_asa_pipeline.model.Identification;

/**
 * Identification wrapper class
 *
 * @author niels
 */
public class IdentificationGuiWrapper {

    public enum ExplanationType {

        UNEXPLAINED, UNMODIFIED, MODIFIED
    }
    /**
     * The identification
     */
    private Identification identification;
    /**
     * The explanation of the identification by the pipeline.
     */
    private ExplanationType explanationType;

    public IdentificationGuiWrapper(Identification identification, ExplanationType explanationType) {
        this.identification = identification;
        this.explanationType = explanationType;
    }

    public Identification getIdentification() {
        return identification;
    }

    public void setIdentification(Identification identification) {
        this.identification = identification;
    }

    public ExplanationType getExplanationType() {
        return explanationType;
    }

    public void setExplanationType(ExplanationType explanationType) {
        this.explanationType = explanationType;
    }
}
