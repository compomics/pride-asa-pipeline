package com.compomics.pride_asa_pipeline.core.logic.inference.ionaccuracy.massdeficit.model.identifications;

import java.math.BigDecimal;

/**
 *
 * @author Kenneth
 */
public class MassDeficitIdentification {

    /**
     * The modified sequence
     */
    private final String modifiedSequence;
    /**
     * The precursor charge
     */
    private final BigDecimal precursorCharge;
    /**
     * The identifier of the scan
     */
    private final String scan_identifier;
    /**
     * The identification score
     */
    private final BigDecimal score;

    public MassDeficitIdentification(String modifiedSequence, String precursorCharge, String scan_identifier, String score) {
        this.modifiedSequence = modifiedSequence;
        this.precursorCharge = new BigDecimal(precursorCharge);
        this.scan_identifier = scan_identifier;
        this.score = new BigDecimal(score);
    }

    public String getModifiedSequence() {
        return modifiedSequence;
    }

    public BigDecimal getPrecursorCharge() {
        return precursorCharge;
    }

    public String getScan_identifier() {
        return scan_identifier;
    }

    public BigDecimal getScore() {
        return score;
    }

}
