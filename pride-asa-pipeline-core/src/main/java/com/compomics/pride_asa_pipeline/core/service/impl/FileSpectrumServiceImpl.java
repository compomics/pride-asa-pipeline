/*
 *

 */
package com.compomics.pride_asa_pipeline.core.service.impl;

import com.compomics.pride_asa_pipeline.core.repository.FileParser;
import com.compomics.pride_asa_pipeline.core.repository.impl.file.FileSpectrumRepository;
import com.compomics.pride_asa_pipeline.model.Peak;
import com.compomics.pride_asa_pipeline.core.service.FileSpectrumService;
import com.sun.accessibility.internal.resources.accessibility;
import java.util.*;
import uk.ac.ebi.pride.utilities.data.controller.impl.ControllerImpl.CachedDataAccessController;

/**
 *
 * @author Niels Hulstaert
 */
public class FileSpectrumServiceImpl implements FileSpectrumService {

    private FileSpectrumRepository spectrumRepository;
    private String assayIdentifier;

    @Override
    public List<Peak> getSpectrumPeaksBySpectrumId(String spectrumId) {
        List<Peak> peaks = new ArrayList<>();
        double[] mzValuesBySpectrumId = spectrumRepository.getMzValuesBySpectrumId(spectrumId);
        double[] intensitiesBySpectrumId = spectrumRepository.getIntensitiesBySpectrumId(spectrumId);
        for (int i = 0; i < mzValuesBySpectrumId.length; i++) {
            peaks.add(new Peak(mzValuesBySpectrumId[i], intensitiesBySpectrumId[i]));
        }
        return peaks;
    }

    @Override
    public HashMap<Double, Double> getSpectrumPeakMapBySpectrumId(String spectrumId) {
        HashMap<Double, Double> peaks = new HashMap<>();
        double[] mzValuesBySpectrumId = spectrumRepository.getMzValuesBySpectrumId(spectrumId);
        double[] intensitiesBySpectrumId = spectrumRepository.getIntensitiesBySpectrumId(spectrumId);
        for (int i = 0; i < mzValuesBySpectrumId.length; i++) {
            peaks.put(mzValuesBySpectrumId[i], intensitiesBySpectrumId[i]);
        }
        return peaks;
    }

    @Override
    public void setActiveAssay(String assayIdentifier) {
        this.assayIdentifier = assayIdentifier;
        this.spectrumRepository=new FileSpectrumRepository(assayIdentifier);
    }

}
