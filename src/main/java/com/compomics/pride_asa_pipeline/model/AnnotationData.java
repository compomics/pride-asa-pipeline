package com.compomics.pride_asa_pipeline.model;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: niels
 * Date: 30/11/11
 * Time: 14:24
 * To change this template use File | Settings | File Templates.
 */
public class AnnotationData {
    
    /**
     * The list of fragment ion annotations of an identification.      
     */
    private List<FragmentIonAnnotation> fragmentIonAnnotations;
    /**
     * The identification scoring result.
     */
    private IdentificationScore identificationScore;
    /**
     * The filter noise threshold
     */
    private double noiseThreshold;
       
    public List<FragmentIonAnnotation> getFragmentIonAnnotations() {
        return fragmentIonAnnotations;
    }

    public void setFragmentIonAnnotations(List<FragmentIonAnnotation> aFragmentIonAnnotations) {
        fragmentIonAnnotations = aFragmentIonAnnotations;
    }

    public IdentificationScore getIdentificationScore() {
        return identificationScore;
    }

    public void setIdentificationScore(IdentificationScore identificationScore) {
        this.identificationScore = identificationScore;
    }

    public double getNoiseThreshold() {
        return noiseThreshold;
    }

    public void setNoiseThreshold(double noiseThreshold) {
        this.noiseThreshold = noiseThreshold;
    }          
    
}
