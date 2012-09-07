/*
 *

 */
package com.compomics.pride_asa_pipeline.service.impl;

import com.compomics.pride_asa_pipeline.config.PropertiesConfigurationHolder;
import com.compomics.pride_asa_pipeline.model.Identification;
import com.compomics.pride_asa_pipeline.model.SpectrumAnnotatorResult;
import com.compomics.pride_asa_pipeline.model.comparator.IdentificationSpectrumIdComparator;
import com.compomics.pride_asa_pipeline.repository.FileResultHandler;
import com.compomics.pride_asa_pipeline.service.ResultHandler;
import java.io.File;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Niels Hulstaert
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

        List<Identification> identifications = spectrumAnnotatorResult.getIdentifications();
        //sort list by spectrum ID
        Collections.sort(identifications, new IdentificationSpectrumIdComparator());

        fileResultHandler.writeResult(resultFile, spectrumAnnotatorResult.getIdentifications());
    }

    @Override
    public SpectrumAnnotatorResult readResultFromFile(File resultFile) {
        return fileResultHandler.readResult(resultFile);
    }
}
