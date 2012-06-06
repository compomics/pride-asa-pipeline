/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pride_asa_pipeline.model;

/**
 *
 * @author niels
 */
public class IdentificationScore {

    /**
     * The number of matched peaks of an identification
     */
    private int matchingPeaks;
    /**
     * The total number of peaks in the spectrum taken into account with the
     * scoring
     */
    private int totalPeaks;
    /**
     * The relative matching itensity of an identification; i.e the intensity of
     * the matched peaks divided by the total intensity
     */
    private long matchingIntensity;
    /**
     * The total intensity of the peaks taken into account with the scoring
     */
    private long totalIntensity;
    /**
     * The peptide length
     */
    private int peptideLength;

    /**
     * Constructor
     *
     * @param matchingPeaks the matching number of peaks value
     * @param totalPeaks the total number of peaks value
     * @param matchingIntensity the matching intensity value
     * @param totalIntensity the total intensity value
     */
    public IdentificationScore(int matchingPeaks, int totalPeaks, long matchingIntensity, long totalIntensity, int peptideLength) {
        this.matchingPeaks = matchingPeaks;
        this.totalPeaks = totalPeaks;
        this.matchingIntensity = matchingIntensity;
        this.totalIntensity = totalIntensity;
        this.peptideLength = peptideLength;
    }

    public int getMatchingPeaks() {
        return matchingPeaks;
    }

    public void setMatchingPeaks(int matchingPeaks) {
        this.matchingPeaks = matchingPeaks;
    }

    public int getTotalPeaks() {
        return totalPeaks;
    }

    public void setTotalPeaks(int totalPeaks) {
        this.totalPeaks = totalPeaks;
    }

    public long getMatchingIntensity() {
        return matchingIntensity;
    }

    public void setMatchingIntensity(long matchingIntensity) {
        this.matchingIntensity = matchingIntensity;
    }

    public long getTotalIntensity() {
        return totalIntensity;
    }

    public void setTotalIntensity(long totalIntensity) {
        this.totalIntensity = totalIntensity;
    }

    public int getPeptideLength() {
        return peptideLength;
    }

    public void setPeptideLength(int peptideLength) {
        this.peptideLength = peptideLength;
    }

    /**
     * Gets the relative matched intensity: i.e. the matched intensity divided
     * by the total intensity
     *
     * @return the relative matched intensity value
     */
    private double getRelativeMatchedIntensity() {
        double relativeMatchedIntensity = (Double.compare(totalIntensity, 0.0) == 0) ? 0.0 : ((double) matchingIntensity) / ((double) totalIntensity);
        return relativeMatchedIntensity;
    }

    /**
     * Gets the average fragment ion score; i.e. the relative matched intensity
     * divided by the number of machted peaks
     *
     * @return the average fragment ion score
     */
    public double getAverageFragmentIonScore() {
        double averageFragmentIonScore = (matchingPeaks == 0) ? 0.0 : getRelativeMatchedIntensity() / ((double) matchingPeaks);
        return averageFragmentIonScore;
    }

    public double getAverageAminoAcidScore() {
        return getRelativeMatchedIntensity() / ((double) peptideLength);
    }
}
