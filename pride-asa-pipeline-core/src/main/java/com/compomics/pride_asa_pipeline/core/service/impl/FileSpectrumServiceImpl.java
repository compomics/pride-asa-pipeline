/*
 *

 */
package com.compomics.pride_asa_pipeline.core.service.impl;

import com.compomics.pride_asa_pipeline.core.repository.FileParser;
import com.compomics.pride_asa_pipeline.model.Peak;
import com.compomics.pride_asa_pipeline.core.service.FileSpectrumService;
import java.util.*;

/**
 *
 * @author Niels Hulstaert
 */
public class FileSpectrumServiceImpl implements FileSpectrumService {

    private FileParser fileParser;       

    @Override
    public void setFileParser(FileParser fileParser) {
        this.fileParser = fileParser;
    }       

    @Override
    public List<Peak> getSpectrumPeaksBySpectrumId(String spectrumId) {
        return fileParser.getSpectrumPeaksBySpectrumId(spectrumId);
    }

    @Override
    public HashMap<Double, Double> getSpectrumPeakMapBySpectrumId(String spectrumId) {
        return fileParser.getSpectrumPeakMapBySpectrumId(spectrumId);
    }

  
}