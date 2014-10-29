/*
 *

 */
package com.compomics.pride_asa_pipeline.model.comparator;

import com.compomics.pride_asa_pipeline.model.Identification;
import java.util.Comparator;

/**
 *
 * @author Niels Hulstaert
 */
public class IdentificationSpectrumIdComparator implements Comparator<Identification> {        
    
    @Override
    public int compare(Identification identification1, Identification identification2) {
        return identification1.getSpectrumId().compareTo(identification2.getSpectrumId());
    }
        
}
