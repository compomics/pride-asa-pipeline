/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pride_asa_pipeline.service.impl;

import com.compomics.pride_asa_pipeline.model.Peak;
import com.compomics.pride_asa_pipeline.repository.SpectrumRepository;
import com.compomics.pride_asa_pipeline.service.SpectrumService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author niels
 */
public class SpectrumServiceImpl implements SpectrumService {

    private SpectrumRepository spectrumRepository;

    public SpectrumRepository getSpectrumRepository() {
        return spectrumRepository;
    }

    public void setSpectrumRepository(SpectrumRepository spectrumRepository) {
        this.spectrumRepository = spectrumRepository;
    }

    @Override
    public List<Peak> getSpectrumPeaksBySpectrumId(long spectrumId) {
        List<Peak> peaks = new ArrayList<Peak>();

        double[] mzValues = spectrumRepository.getMzValuesBySpectrumId(spectrumId);
        double[] intensities = spectrumRepository.getIntensitiesBySpectrumId(spectrumId);

        for (int i = 0; i < mzValues.length; i++) {
            Peak peak = new Peak(mzValues[i], intensities[i]);
            peaks.add(peak);
        }

        return peaks;
    }

    @Override
    public HashMap<Double, Double> getSpectrumPeakMapBySpectrumId(long spectrumId) {
        HashMap<Double, Double> peaks = new HashMap<Double, Double>();

        double[] mzValues = spectrumRepository.getMzValuesBySpectrumId(spectrumId);
        double[] intensities = spectrumRepository.getIntensitiesBySpectrumId(spectrumId);

        for (int i = 0; i < mzValues.length; i++) {
            peaks.put(mzValues[i], intensities[i]);
        }

        return peaks;
    }
}
