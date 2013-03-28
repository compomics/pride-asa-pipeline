package com.compomics.pride_asa_pipeline.model;

/**
 * @author Florian Reisinger
 *         Date: 08-Aug-2009
 * @since 0.1
 */
public class AnalyzerData {
    
    public static enum ANALYZER_FAMILY {

        IONTRAP,
        TOF,
        FT,
        ORBITRAP,
        UNKNOWN
    }
    
    //instrument mass error on precursor ions
    private Double precursorMassError; 
    //instrument mass error on fragment ions
    private Double fragmentMassError;  
    //instrument identifier/name
    private ANALYZER_FAMILY analyzerFamily;               

    public AnalyzerData(Double precursorMassError, Double fragmentMassError, ANALYZER_FAMILY analyzerFamily) {
        this.precursorMassError = precursorMassError;
        this.fragmentMassError = fragmentMassError;
        this.analyzerFamily = analyzerFamily;
    }

    public Double getPrecursorMassError() {
        return precursorMassError;
    }

    public Double getFragmentMassError() {
        return fragmentMassError;
    }

    public ANALYZER_FAMILY getAnalyzerFamily() {
        return analyzerFamily;
    }
    
    @Override
    public String toString() {
        return "AnalyzerData{" +
                "precursorMassError=" + precursorMassError +
                ", fragmentMassError=" + fragmentMassError +
                ", analyzer family='" + analyzerFamily.toString() + '\'' +
                '}';
    }
}
