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
package com.compomics.pride_asa_pipeline.core.util;

import com.compomics.pride_asa_pipeline.model.Peak;
import java.util.List;

/**
 *
 * @author Niels Hulstaert
 */
public class PeakUtils {

    /**
     * Gets the spectrum mz ratios as double array
     *
     * @param peaks the spectrum peaks
     * @return the spectrum mz ratios double array
     */
    public static double[] getMzRatiosAsArray(List<Peak> peaks) {
        if (peaks == null) {
            return null;
        }
        double[] intensities = new double[peaks.size()];
        for (int i = 0; i < peaks.size(); i++) {
            intensities[i] = peaks.get(i).getMzRatio();

        }
        return intensities;
    }

    /**
     * Gets the spectrum intensities as double array
     *
     * @param peaks the spectrum peaks
     * @return the spectrum intensities double array
     */
    public static double[] getIntensitiesAsArray(List<Peak> peaks) {
        if (peaks == null) {
            return null;
        }
        double[] intensities = new double[peaks.size()];
        for (int i = 0; i < peaks.size(); i++) {
            intensities[i] = peaks.get(i).getIntensity();

        }
        return intensities;
    }

    /**
     * Determines if the provided m/z of the fragment ion matches the proviced
     * m/z of the spectra spectrum m/z. Note: This will take a fragment mass
     * measurement error into account. Therefore the fragment m/z is regarded as
     * 'hit' if it falls within the interval of (spectra m/z -
     * fragmentMassError) to (spectra m/z + fragmentMassError).
     *
     * @param spectraMz the signal spectrum m/z from the spectra.
     * @param fragmentMz the m/z of the fragment ion.
     * @param fragmentMassError the fragment mass error.
     * @return true if the fragment m/z 'hits' (with a given error tolerance)
     * the signal m/z.
     */
    public static boolean isHit(double spectraMz, double fragmentMz, double fragmentMassError) {
        //return true if the fragmentMZ is in the interval (around the spectrum spectrum MZ)
        //that is defined by the fragment mass error.
        return (fragmentMz > spectraMz - fragmentMassError) && (fragmentMz < spectraMz + fragmentMassError);
    }
}
