package com.compomics.pride_asa_pipeline.logic.spectrum.filter;

import com.compomics.pride_asa_pipeline.model.Peak;
import java.util.List;

/**
 * @author Florian Reisinger Date: 08-Oct-2009
 * @since 0.1
 */
public interface NoiseFilter {

    /**
     * Filters the peak list; all peaks that have an instensity lower than the
     * given threshold will be omitted. The experimental precursor mass is given
     * as a method argument to exclude this mz value from the filtered spectrum.
     *
     * @param peaks the list of peaks
     * @param threshold the threshold value
     * @param experimentalPrecursorMass the experimental precursor mass value
     * @return the filtered peak list
     */
    public List<Peak> filterNoise(List<Peak> peaks, double threshold, double experimentalPrecursorMass);
}
