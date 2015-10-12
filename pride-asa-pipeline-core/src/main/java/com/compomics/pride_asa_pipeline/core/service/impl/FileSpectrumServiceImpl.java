/*
 *

 */
package com.compomics.pride_asa_pipeline.core.service.impl;

import com.compomics.pride_asa_pipeline.core.repository.FileParser;
import com.compomics.pride_asa_pipeline.core.service.FileSpectrumService;
import com.compomics.pride_asa_pipeline.model.Peak;
import java.util.HashMap;
import java.util.List;

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

    public FileParser getFileParser(){
        return this.fileParser;
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
