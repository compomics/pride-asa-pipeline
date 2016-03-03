package com.compomics.pride_asa_pipeline.core.logic.inference.ionaccuracy.massdeficit.model;

import java.math.BigDecimal;

/**
 *
 * @author Kenneth
 */
public class MassMatch {

    /**
     * The theoretical mass to charge for the match
     */
    private final BigDecimal theoreticalMz;
    /**
     * The observed mass to charge for the match
     */
    private final BigDecimal observedMz;
    /**
     * The peptide charge
     */
    private final BigDecimal charge;
    /**
     * The measured mass error
     */
    private final BigDecimal massError;

    public MassMatch(BigDecimal theoreticalMz, BigDecimal observedMz, BigDecimal charge) {
        this.theoreticalMz = theoreticalMz;
        this.observedMz = observedMz;
        this.charge = charge;
        this.massError = observedMz.subtract(theoreticalMz);
    }

    public BigDecimal getTheoreticalMz() {
        return theoreticalMz;
    }

    public BigDecimal getObservedMz() {
        return observedMz;
    }

    public BigDecimal getCharge() {
        return charge;
    }

    public BigDecimal getMassError() {
        return massError;
    }

    /**
     * compares this mass match to another and returns a boolean if it fits better
     * @param other the mass match to compare to
     * @return a boolean indicating the fit is better (smaller error)
     */
    public boolean isBetterMatch(MassMatch other) {
        return other.getMassError().compareTo(getMassError()) == 1;
    }

}
