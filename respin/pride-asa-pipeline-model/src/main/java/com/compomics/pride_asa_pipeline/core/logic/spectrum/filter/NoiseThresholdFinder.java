package com.compomics.pride_asa_pipeline.core.logic.spectrum.filter;

/**
 * @author Florian Reisinger
 *         Date: 10-Sep-2009
 * @since 0.1
 */
public interface NoiseThresholdFinder {
    
    /**
     * Finds the noise threshold for an array of signal values
     * 
     * @param signalValues the double array of signal values
     * @return the double threshold value
     */
    public double findNoiseThreshold(double[] signalValues);

}
