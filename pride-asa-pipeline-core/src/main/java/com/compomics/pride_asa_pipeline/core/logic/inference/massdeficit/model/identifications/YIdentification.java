package com.compomics.pride_asa_pipeline.core.logic.inference.massdeficit.model.identifications;

import com.compomics.pride_asa_pipeline.model.Peak;
import java.util.Collection;

/**
 *
 * @author Kenneth
 */
public class YIdentification extends Identification{
    private final Collection<Peak> peaks;
    
    
    
   public YIdentification(String modifiedSequence, String precursorCharge, String scan_identifier, String score,Collection<Peak> annotatedPeaks) {
       super(modifiedSequence, precursorCharge, scan_identifier, score);
       this.peaks = annotatedPeaks;
   }

    public Collection<Peak> getPeaks() {
        return peaks;
    }
    
    
}
