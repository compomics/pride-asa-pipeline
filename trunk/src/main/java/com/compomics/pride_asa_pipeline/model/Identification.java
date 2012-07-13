package com.compomics.pride_asa_pipeline.model;

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
    private long spectrumId;
    /**
     * The pride spectrum reference
     */
    private long spectrumRef;
    /**
     * The annotation data
     */
    private AnnotationData annotationData;

    public Identification(Peptide peptide, String mzAccession, long spectrumId, long spectrumRef) {
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

    public long getSpectrumId() {
        return spectrumId;
    }

    public long getSpectrumRef() {
        return spectrumRef;
    }

    public AnnotationData getAnnotationData() {
        return annotationData;
    }

    public void setAnnotationData(AnnotationData annotationData) {
        this.annotationData = annotationData;
    }
    
    public void setPeptide(Peptide peptide){
        this.peptide = peptide;
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
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Identification that = (Identification) o;

        if (spectrumId != that.spectrumId) {
            return false;
        }
        if (spectrumRef != that.spectrumRef) {
            return false;
        }
        if (mzAccession != null ? !mzAccession.equals(that.mzAccession) : that.mzAccession != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = peptide != null ? peptide.hashCode() : 0;
        result = 31 * result + (mzAccession != null ? mzAccession.hashCode() : 0);
        result = 31 * result + (int) (spectrumId ^ (spectrumId >>> 32));
        result = 31 * result + (int) (spectrumRef ^ (spectrumRef >>> 32));
        return result;
    }
}
