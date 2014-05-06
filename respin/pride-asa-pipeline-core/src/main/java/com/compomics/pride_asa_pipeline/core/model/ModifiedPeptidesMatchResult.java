/*
 *

 */
package com.compomics.pride_asa_pipeline.core.model;

import com.compomics.pride_asa_pipeline.model.AnnotationData;
import com.compomics.pride_asa_pipeline.model.ModifiedPeptide;

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
