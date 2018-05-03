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

/**
 * @author Florian Reisinger
 *         Date: 10-Sep-2009
 * @since 0.1
 */
public interface NoiseThresholdFinder {
    
    /**
     * Finds the noise threshold for an array of signal values
     * 
     * @param signalValues the double array of signal values
     * @return the double threshold value
     */
    public double findNoiseThreshold(double[] signalValues);

}
