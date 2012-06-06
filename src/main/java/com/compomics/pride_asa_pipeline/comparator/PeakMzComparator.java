package com.compomics.pride_asa_pipeline.comparator;

import com.compomics.pride_asa_pipeline.model.Peak;
import java.util.Comparator;

/**
 * @author Florian Reisinger
 *         Date: 15-Jun-2010
 * @since $version
 */
public class PeakMzComparator implements Comparator<Peak> {

    @Override
    public int compare(Peak p1, Peak p2) {
        //compare the two provided peaks according to there m/z value
        return Double.compare(p1.getMzRatio(), p2.getMzRatio());
    }
}
