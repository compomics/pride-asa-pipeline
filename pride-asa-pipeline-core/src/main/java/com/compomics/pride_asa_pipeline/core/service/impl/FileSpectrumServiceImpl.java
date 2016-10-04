/*
 *

 */
package com.compomics.pride_asa_pipeline.core.service.impl;

import com.compomics.pride_asa_pipeline.core.repository.FileParser;
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

    private FileParser fileParser;       
    private String assayIdentifier;


    @Override
    public List<Peak> getSpectrumPeaksBySpectrumId(String spectrumId) {
        return fileParser.getSpectrumPeaksBySpectrumId(spectrumId);
    }

    @Override
    public HashMap<Double, Double> getSpectrumPeakMapBySpectrumId(String spectrumId) {
        return fileParser.getSpectrumPeakMapBySpectrumId(spectrumId);
    }


    @Override
    public void setActiveAssay(String assayIdentifier) {
        this.assayIdentifier=assayIdentifier;
    }

  
}