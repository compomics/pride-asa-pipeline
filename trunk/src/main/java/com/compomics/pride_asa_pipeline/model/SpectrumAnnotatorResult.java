/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pride_asa_pipeline.model;

import com.compomics.pride_asa_pipeline.model.comparator.IdentificationComparator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Convenience class for holding the pipeline results
 *
 * @author Niels Hulstaert
 */
public class SpectrumAnnotatorResult {

    /**
     * The experiment accession
     */
    String experimentAccession;
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

    public SpectrumAnnotatorResult(String experimentAccession) {
        this();
        this.experimentAccession = experimentAccession;
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

    public String getExperimentAccession() {
        return experimentAccession;
    }        

    /**
     * Returns all the experiment identications as a list, sorted by spectrum
     * ID.
     *
     * @return the list of experiment identifications
     */
    public List<Identification> getIdentifications() {
        List<Identification> identifications = new ArrayList<Identification>();
        identifications.addAll(unmodifiedPrecursors);
        identifications.addAll(modifiedPrecursors);
        identifications.addAll(unexplainedIdentifications);

        Collections.sort(identifications, new IdentificationComparator());
        
        return identifications;
    }
    
    /**
     * Gets the total number of identifications (unexplained, unmodified and modified).
     * 
     * @return the total number of identifications
     */
    public int getNumberOfIdentifications(){
        return unexplainedIdentifications.size() + unmodifiedPrecursors.size() + modifiedPrecursors.size();
    }
    
}
