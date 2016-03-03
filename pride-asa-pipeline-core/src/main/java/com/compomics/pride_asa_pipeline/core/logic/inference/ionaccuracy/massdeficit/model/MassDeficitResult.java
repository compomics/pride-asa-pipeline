package com.compomics.pride_asa_pipeline.core.logic.inference.ionaccuracy.massdeficit.model;

import java.math.BigDecimal;

/**
 *
 * @author Kenneth
 */
public class MassDeficitResult {
    /**
     * The targeted amino acid
     */
    private final String aminoAcid;
    /**
     * THe observed mass to charge
     */
    private final BigDecimal observedMz;
    /**
     * The mz gap to the next amino acid
     */
    private final BigDecimal gapToNext;
    /**
     * The mass deficit between this amino acid and the next
     */
    private final BigDecimal massDeficit;
    /**
     * The deficit to mass ratio
     */
    private final BigDecimal deficitToMassRatio;
    /**
     * The gap in the mass ratio (to the next amino acid)
     */
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
