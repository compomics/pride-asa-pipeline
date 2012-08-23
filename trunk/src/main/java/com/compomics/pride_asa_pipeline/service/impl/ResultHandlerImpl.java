/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pride_asa_pipeline.service.impl;

import com.compomics.pride_asa_pipeline.config.PropertiesConfigurationHolder;
import com.compomics.pride_asa_pipeline.model.SpectrumAnnotatorResult;
import com.compomics.pride_asa_pipeline.repository.FileResultHandler;
import com.compomics.pride_asa_pipeline.service.ResultHandler;
import java.io.File;

/**
 *
 * @author niels
 */
public class ResultHandlerImpl implements ResultHandler {
    
    private FileResultHandler fileResultHandler;

    public FileResultHandler getFileResultHandler() {
        return fileResultHandler;
    }

    public void setFileResultHandler(FileResultHandler fileResultHandler) {
        this.fileResultHandler = fileResultHandler;
    }        

    @Override
    public void writeResultToFile(SpectrumAnnotatorResult spectrumAnnotatorResult) {
        File resultFile = new File(PropertiesConfigurationHolder.getInstance().getString("results_path"), spectrumAnnotatorResult.getExperimentAccession() + ".txt");

        fileResultHandler.writeResult(resultFile, spectrumAnnotatorResult.getIdentifications());
    }
}
