/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pride_asa_pipeline.model;

import java.util.List;

/**
 * Helper class for storing the peak filtering result: the filtered peaks + the
 * used noise filter threshold.
 *
 * @author Niels Hulstaert
 */
public class PeakFilterResult {

    private List<Peak> filteredPeaks;
    private double noiseThreshold;

    public PeakFilterResult(List<Peak> filteredPeaks) {
        this.filteredPeaks = filteredPeaks;
        noiseThreshold = 0.0;
    }
    
    public PeakFilterResult(List<Peak> filteredPeaks, double noiseThreshold) {
        this.filteredPeaks = filteredPeaks;
        this.noiseThreshold = noiseThreshold;
    }

    public List<Peak> getFilteredPeaks() {
        return filteredPeaks;
    }

    public void setFilteredPeaks(List<Peak> filteredPeaks) {
        this.filteredPeaks = filteredPeaks;
    }

    public double getNoiseThreshold() {
        return noiseThreshold;
    }

    public void setNoiseThreshold(double noiseThreshold) {
        this.noiseThreshold = noiseThreshold;
    }
}
