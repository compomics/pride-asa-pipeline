package com.compomics.pride_asa_pipeline.spectrum.filter.impl;

import com.compomics.pride_asa_pipeline.model.Peak;
import com.compomics.pride_asa_pipeline.spectrum.filter.NoiseFilter;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Florian Reisinger
 *         Date: 10-Sep-2009
 * @since 0.1
 */
public class PeakNoiseFilter implements NoiseFilter {

    @Override
    public List<Peak> filterNoise(List<Peak> peaks, double threshold, double experimentalPrecursorMass) {
        if (peaks == null) {
            return null;
        }
        List<Peak> result = new ArrayList<Peak>();
        for (Peak peak : peaks) {
            //add the peak to the peak list if the peak intensity > threshold
            // and if the MZ ratio is not in 18D range of experimental precursor mass
            if (peak.getIntensity() >= threshold && !(experimentalPrecursorMass - 18 < peak.getMzRatio() && peak.getMzRatio() < experimentalPrecursorMass + 18)) {
                result.add(peak);
            }
        }
        return result;
    }
}
