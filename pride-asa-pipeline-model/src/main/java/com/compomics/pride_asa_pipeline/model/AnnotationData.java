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
    private double noiseThreshold = 0.0;
       
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
