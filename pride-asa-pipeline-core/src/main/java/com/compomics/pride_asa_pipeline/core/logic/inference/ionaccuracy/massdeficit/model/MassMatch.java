/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pride_asa_pipeline.core.logic.inference.ionaccuracy.massdeficit.model;

import java.math.BigDecimal;

/**
 *
 * @author Kenneth
 */
public class MassMatch {

    private final BigDecimal theoreticalMz;
    private final BigDecimal observedMz;
    private final BigDecimal charge;
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

    public boolean isBetterMatch(MassMatch other) {
        return other.getMassError().compareTo(getMassError()) == 1;
    }

}
