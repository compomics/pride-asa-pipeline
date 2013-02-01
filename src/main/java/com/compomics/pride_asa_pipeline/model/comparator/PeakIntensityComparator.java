package com.compomics.pride_asa_pipeline.model.comparator;

import com.compomics.pride_asa_pipeline.model.Peak;
import java.util.Comparator;

/**
 * @author Florian Reisinger
 *         Date: 15-Jun-2010
 * @since $version
 */
public class PeakIntensityComparator implements Comparator<Peak> {
    
    @Override
    public int compare(Peak p1, Peak p2) {
        //compare the two provided peaks according to their intensities
        //inverse the 'usual' double comparison to achieve 'high to low' instead of 'low to high' ordering  
        return -1 * Double.compare(p1.getIntensity(), p2.getIntensity());
    }

}
