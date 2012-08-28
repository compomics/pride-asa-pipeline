/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pride_asa_pipeline.model;

/**
 * Helper class for storing the modified peptides matching result: the best
 * modified peptide (if found) + the annotation data
 *
 * @author Niels Hulstaert
 */
public class ModifiedPeptidesMatchResult {

    private ModifiedPeptide modifiedPeptide;
    private AnnotationData annotationData;

    public ModifiedPeptidesMatchResult(ModifiedPeptide modifiedPeptide, AnnotationData annotationData) {
        this.modifiedPeptide = modifiedPeptide;
        this.annotationData = annotationData;
    }

    public ModifiedPeptide getModifiedPeptide() {
        return modifiedPeptide;
    }

    public void setModifiedPeptide(ModifiedPeptide modifiedPeptide) {
        this.modifiedPeptide = modifiedPeptide;
    }

    public AnnotationData getAnnotationData() {
        return annotationData;
    }

    public void setAnnotationData(AnnotationData annotationData) {
        this.annotationData = annotationData;
    }
}
