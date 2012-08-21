/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pride_asa_pipeline.service.impl;

import com.compomics.pride_asa_pipeline.config.PropertiesConfigurationHolder;
import com.compomics.pride_asa_pipeline.model.SpectrumAnnotatorResult;
import com.compomics.pride_asa_pipeline.repository.ResultWriter;
import com.compomics.pride_asa_pipeline.service.ResultService;
import java.io.File;
import org.apache.log4j.Logger;

/**
 *
 * @author niels
 */
public class ResultServiceImpl implements ResultService {
    
    private static final Logger LOGGER = Logger.getLogger(ResultServiceImpl.class);
    
    private ResultWriter resultWriter;

    public ResultWriter getResultWriter() {
        return resultWriter;
    }

    public void setResultWriter(ResultWriter resultWriter) {
        this.resultWriter = resultWriter;
    }        
    
    @Override
    public void writeResultToFile(SpectrumAnnotatorResult spectrumAnnotatorResult) {
        File resultFile = new File(PropertiesConfigurationHolder.getInstance().getString("results_path"), spectrumAnnotatorResult.getExperimentAccession() + ".txt");
        
        resultWriter.writeResult(resultFile, spectrumAnnotatorResult.getIdentifications());
    }
            
}
