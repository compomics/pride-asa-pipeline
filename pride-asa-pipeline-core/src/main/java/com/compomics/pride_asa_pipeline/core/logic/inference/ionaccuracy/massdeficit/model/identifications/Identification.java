package com.compomics.pride_asa_pipeline.core.logic.inference.ionaccuracy.massdeficit.model.identifications;

import java.math.BigDecimal;

/**
 *
 * @author Kenneth
 */
public class Identification {

    private final String modifiedSequence;
    private final BigDecimal precursorCharge;
    private final String scan_identifier;
    private final BigDecimal score;

    public Identification(String modifiedSequence, String precursorCharge, String scan_identifier, String score) {
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
