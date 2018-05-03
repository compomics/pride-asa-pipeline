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
package com.compomics.pride_asa_pipeline.core.repository;

import com.compomics.pride_asa_pipeline.model.Peak;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Niels Hulstaert
 */

/**
 *
 * @author Kenneth Verheggen
 */
public interface SpectrumRepository {
    
    /**
     * Gets the spectrum mz values array as double array
     * 
     * @param spectrumId the spectrum ID
     * @return the mz values double array
     */
    double[] getMzValuesBySpectrumId(String spectrumId);    
    
    /**
     * Gets the spectrum intensities array as double array
     * 
     * @param spectrumId the spectrum ID
     * @return the intensities double array
     */
    double[] getIntensitiesBySpectrumId(String spectrumId);

    /**
     * Gets Maps with spectrumid:peakMap structure
     *
     * @param spectrumIds the spectrum IDs
     * @return the Map with spectrumIds as keys, and Maps (mass:intensity) as values
     */
    Map<String, List<Peak>> getPeakMapsBySpectrumIdList(List<String> spectrumIds);

    
}
