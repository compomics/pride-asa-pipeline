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
package com.compomics.pride_asa_pipeline.core.logic.spectrum.filter;

import com.compomics.pride_asa_pipeline.model.Peak;
import java.util.List;

/**
 * @author Florian Reisinger Date: 08-Oct-2009
 * @since 0.1
 */
public interface NoiseFilter {

    /**
     * Filters the peak list; all peaks that have an intensity lower than the
     * given threshold will be omitted. The experimental precursor m/z ratio is given
     * as a method argument to exclude this mz value from the filtered spectrum.
     *
     * @param peaks the list of peaks
     * @param threshold the threshold value
     * @param experimentalPrecursorMzRatio the experimental precursor m/z ratio value
     * @return the filtered peak list
     */
    public List<Peak> filterNoise(List<Peak> peaks, double threshold, double experimentalPrecursorMzRatio);
}
