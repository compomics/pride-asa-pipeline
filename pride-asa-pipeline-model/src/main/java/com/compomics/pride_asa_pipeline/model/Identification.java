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

import java.util.Objects;
import org.apache.log4j.Logger;


/**
 * @author Florian Reisinger Date: 20-Aug-2009
 * @since 0.1
 */
public class Identification {

    private static final Logger LOGGER = Logger.getLogger(Identification.class);
    /*
     * The precursor peptide
     */
    private Peptide peptide;
    /**
     * The pride mz data accession
     */
    private String mzAccession;
    /**
     * The pride spectrum ID
     */
    private String spectrumId;
    /**
     * The pride spectrum reference
     */
    private String spectrumRef;
    /**
     * The annotation data
     */
    private AnnotationData annotationData;
    /**
     * the pipeline explanation for this identification
     */
    private PipelineExplanationType pipelineExplanationType;

    public Identification(){}
    
    public Identification(Peptide peptide, String mzAccession, String spectrumId, String spectrumRef) {
        this.peptide = peptide;
        this.mzAccession = mzAccession;
        this.spectrumId = spectrumId;
        this.spectrumRef = spectrumRef;
    }

    public Peptide getPeptide() {
        return peptide;
    }

    public String getMzAccession() {
        return mzAccession;
    }

    public String getSpectrumId() {
        return spectrumId;
    }

    public String getSpectrumRef() {
        return spectrumRef;
    }

    public AnnotationData getAnnotationData() {
        return annotationData;
    }

    public void setAnnotationData(AnnotationData annotationData) {
        this.annotationData = annotationData;
    }

    public void setPeptide(Peptide peptide) {
        this.peptide = peptide;
    }

    public PipelineExplanationType getPipelineExplanationType() {
        return pipelineExplanationType;
    }

    public void setPipelineExplanationType(PipelineExplanationType pipelineExplanationType) {
        this.pipelineExplanationType = pipelineExplanationType;
    }
        
    public String toShortString() {
        return new StringBuilder().append("IdentificationData{").append("mzAccession='").append(mzAccession).append('\'').append(", spectrumId=").append(spectrumId).append(", spectrumRef=").append(spectrumRef).append('}').toString();
    }

    @Override
    public String toString() {
        return "IdentificationData{"
                + "peptide=" + peptide
                + ", mzAccession='" + mzAccession + '\''
                + ", spectrumId=" + spectrumId
                + ", spectrumRef=" + spectrumRef
                + '}';
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 17 * hash + Objects.hashCode(this.mzAccession);
        hash = 17 * hash + Objects.hashCode(this.spectrumId);
        hash = 17 * hash + Objects.hashCode(this.spectrumRef);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Identification other = (Identification) obj;
        if (!Objects.equals(this.mzAccession, other.mzAccession)) {
            return false;
        }
        if (!Objects.equals(this.spectrumId, other.spectrumId)) {
            return false;
        }
        if (!Objects.equals(this.spectrumRef, other.spectrumRef)) {
            return false;
        }
        return true;
    }    
}
