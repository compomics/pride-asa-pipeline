/* 
 * Copyright 2018 compomics.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
