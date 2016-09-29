/*
 *

 */
package com.compomics.pride_asa_pipeline.core.logic;

import com.compomics.pride_asa_pipeline.core.logic.recalibration.MassRecalibrationResult;
import com.compomics.pride_asa_pipeline.model.Identification;
import com.compomics.pride_asa_pipeline.model.PipelineExplanationType;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Convenience class for holding the pipeline results
 *
 * @author Niels Hulstaert Hulstaert
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
     * The mapping of identifications to their explanationtype
     */
    private Map<PipelineExplanationType, List<Identification>> identifications;

    public SpectrumAnnotatorResult() {
        identifications = new EnumMap<>(PipelineExplanationType.class);
        identifications.put(PipelineExplanationType.UNEXPLAINED, new ArrayList<Identification>());
        identifications.put(PipelineExplanationType.UNMODIFIED, new ArrayList<Identification>());
        identifications.put(PipelineExplanationType.MODIFIED, new ArrayList<Identification>());
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
        return identifications.get(PipelineExplanationType.MODIFIED);
    }

    public void setModifiedPrecursors(List<Identification> modifiedPrecursors) {
        identifications.put(PipelineExplanationType.MODIFIED, modifiedPrecursors);
    }

    public List<Identification> getUnexplainedIdentifications() {
        return identifications.get(PipelineExplanationType.UNEXPLAINED);
    }

    public void setUnexplainedIdentifications(List<Identification> unexplainedIdentifications) {
        identifications.put(PipelineExplanationType.UNEXPLAINED, unexplainedIdentifications);
    }

    public List<Identification> getUnmodifiedPrecursors() {
        return identifications.get(PipelineExplanationType.UNMODIFIED);
    }

    public void setUnmodifiedPrecursors(List<Identification> unmodifiedPrecursors) {
        identifications.put(PipelineExplanationType.UNMODIFIED, unmodifiedPrecursors);
    }

    public String getExperimentAccession() {
        return experimentAccession;
    }

    /**
     * Returns all the experiment identications as a list.
     *
     * @return the list of experiment identifications
     */
    public List<Identification> getIdentifications() {
        List<Identification> allIdentifications = new ArrayList<>();
        allIdentifications.addAll(getUnexplainedIdentifications());
        allIdentifications.addAll(getUnmodifiedPrecursors());
        allIdentifications.addAll(getModifiedPrecursors());

        return allIdentifications;
    }

    /**
     * Gets the total number of identifications (unexplained, unmodified and
     * modified).
     *
     * @return the total number of identifications
     */
    public int getNumberOfIdentifications() {
        return getUnexplainedIdentifications().size() + getUnmodifiedPrecursors().size() + getModifiedPrecursors().size();
    }

    /**
     * Adds an identification to identifcations
     *
     * @param identification the identification
     */
    public void addIdentification(Identification identification) {
        if (identification.getPipelineExplanationType().equals(PipelineExplanationType.UNEXPLAINED)) {
            identifications.get(PipelineExplanationType.UNEXPLAINED).add(identification);
        } else if (identification.getPipelineExplanationType().equals(PipelineExplanationType.UNMODIFIED)) {
            identifications.get(PipelineExplanationType.UNMODIFIED).add(identification);
        } else {
            identifications.get(PipelineExplanationType.MODIFIED).add(identification);
        }
    }
}
