/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pride_asa_pipeline.util;

import com.compomics.pride_asa_pipeline.model.Peak;
import java.util.List;

/**
 *
 * @author niels
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
     * @see this #fragmentMassError
     */
    public static boolean isHit(double spectraMz, double fragmentMz, double fragmentMassError) {
        //return true if the fragmentMZ is in the interval (around the spectrum spectrum MZ)
        //that is defined by the fragment mass error.
        return (fragmentMz > spectraMz - fragmentMassError) && (fragmentMz < spectraMz + fragmentMassError);
    }
}
