package com.compomics.pride_asa_pipeline.logic.spectrum.filter.impl;

import com.compomics.pride_asa_pipeline.logic.spectrum.filter.NoiseFilter;
import com.compomics.pride_asa_pipeline.model.Peak;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Florian Reisinger
 *         Date: 10-Sep-2009
 * @since 0.1
 */
public class PeakNoiseFilter implements NoiseFilter {
    
    private static final double PRECURSOR_MASS_WINDOW = 18.0;

    @Override
    public List<Peak> filterNoise(List<Peak> peaks, double threshold, double experimentalPrecursorMzRatio) {
        if (peaks == null) {
            return null;
        }
        List<Peak> result = new ArrayList<Peak>();
        for (Peak peak : peaks) {
            //add the peak to the peak list if the peak intensity > threshold
            //and if the MZ ratio is not in 18D range of experimental precursor mass
            if (peak.getIntensity() >= threshold && !(experimentalPrecursorMzRatio - PRECURSOR_MASS_WINDOW < peak.getMzRatio() && peak.getMzRatio() < experimentalPrecursorMzRatio + PRECURSOR_MASS_WINDOW)) {
                result.add(peak);
            }
        }
        return result;
    }
}
