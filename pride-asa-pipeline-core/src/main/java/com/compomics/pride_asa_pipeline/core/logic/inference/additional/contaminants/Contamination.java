package com.compomics.pride_asa_pipeline.core.logic.inference.additional.contaminants;

/**
 * This class represents technical contaminations in a spectrum.
 *
 * @author Kenneth Verheggen
 */
public class Contamination {

    /**
     * Returns the headers for a report
     *
     * @return the headers for a report
     */
    public static String getHeader() {
        return "Name\tSequence\tObserved\tTheoretical\tDelta\tValidated\tIon_Type";
    }

    /**
     * The peptide sequence that was linked to the identification
     */
    private final String sequence;
    /**
     * The name of the mass that was explained (ergo the name of the
     * contaminant)
     */
    private final String name;
    /**
     * The observed mass difference
     */
    private final double observed;
    /**
     * The theoretical (average) mass difference
     */
    private final double theoretical;
    /**
     * Boolean indicating if the contaminant was found on the precursor or
     * fragment level
     */
    private final boolean isFragmentIon;

    /**
     * Creates a new contaminant hit for previously unexplained masses
     *
     * @param name the name of the contaminant
     * @param sequence the peptide sequence that was linked to the
     * identification
     * @param observed the observed mass difference
     * @param theoretical the theoretical mass difference for the contamination
     * @param isFragmentIon a boolean indicating the level of the contamination
     * (MS1 / MS2)
     */
    public Contamination(String name, String sequence, double observed, double theoretical, boolean isFragmentIon) {
        this.isFragmentIon = isFragmentIon;
        this.name = name;
        this.sequence = sequence;
        this.observed = observed;
        this.theoretical = theoretical;
    }

    /**
     *
     * @param massTolerance the allowed mass tolerance
     * @return boolean indicating whether the newly explained mass difference
     * also matches the provided mass tolerance
     */
    public boolean isConfident(double massTolerance) {
        return Math.abs(observed - theoretical) <= massTolerance;
    }

    public String getName() {
        return name;
    }

    public String getSequence() {
        return sequence;
    }

    public double getObserved() {
        return observed;
    }

    public double getTheoretical() {
        return theoretical;
    }

    public boolean isFragmentIon() {
        return isFragmentIon;
    }

    @Override
    public String toString() {
        return toString(1.0);
    }

    /**
     * Returns a report line fitting the provided mass tolerance
     *
     * @param massTolerance the mass tolerance. Can be either for precursor or
     * for fragments
     * @return a report line fitting the provided mass tolerance
     */
    public String toString(double massTolerance) {
        String toString = getName() + "\t"
                + getSequence() + "\t"
                + getObserved() + "\t"
                + getTheoretical() + "\t"
                + Math.abs(getObserved() - getTheoretical()) + "\t";
        if (isConfident(massTolerance)) {
            toString += "YES";
        } else {
            toString += "NO";
        }
        toString += "\t";
        if (isFragmentIon) {
            toString += "FRAGMENT ION";
        } else {
            toString += "PRECURSOR ION";
        }
        return toString;
    }

}
