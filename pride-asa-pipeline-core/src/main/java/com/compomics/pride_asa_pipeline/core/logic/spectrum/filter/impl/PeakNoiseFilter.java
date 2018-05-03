/* 
 * Copyright 2018 compomics.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.compomics.pride_asa_pipeline.core.logic.spectrum.filter.impl;

import com.compomics.pride_asa_pipeline.core.logic.spectrum.filter.NoiseFilter;
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
