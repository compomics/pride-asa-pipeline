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
public class MassDeficitResult {
    private final String aminoAcid;
    private final BigDecimal observedMz;
    private final BigDecimal gapToNext;
    private final BigDecimal massDeficit;
    private final BigDecimal deficitToMassRatio;
    private final BigDecimal massRatioGap;
    
    
public MassDeficitResult(String aminoAcid ,BigDecimal observedMz,BigDecimal gapToNext,BigDecimal massDeficit,BigDecimal deficitToMassRatio, BigDecimal massRatioGap){
    this.aminoAcid =aminoAcid;
    this.observedMz=observedMz;
    this.gapToNext=gapToNext;
    this.massDeficit=massDeficit;
    this.deficitToMassRatio = deficitToMassRatio;
    this.massRatioGap=massRatioGap;
}    

    public String getAminoAcid() {
        return aminoAcid;
    }

    public BigDecimal getObservedMz() {
        return observedMz;
    }

    public BigDecimal getGapToNext() {
        return gapToNext;
    }

    public BigDecimal getMassDeficit() {
        return massDeficit;
    }

    public BigDecimal getDeficitToMassRatio() {
        return deficitToMassRatio;
    }

    public BigDecimal getMassRatioGap() {
        return massRatioGap;
    }
    
    @Override
    public String toString(){
        return (getAminoAcid()+ "\t" + getObservedMz() + "\t" + getGapToNext() + "\t" + getMassDeficit() + "\t" + getDeficitToMassRatio() + "\t" + getMassRatioGap());
                    
    }


}
