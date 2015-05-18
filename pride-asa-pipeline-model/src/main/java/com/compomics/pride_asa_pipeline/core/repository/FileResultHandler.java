/*
 *

 */
package com.compomics.pride_asa_pipeline.core.repository;

import com.compomics.pride_asa_pipeline.model.Identification;
import com.compomics.pride_asa_pipeline.core.model.SpectrumAnnotatorResult;
import java.io.File;
import java.util.List;

/**
 *
 * @author Niels Hulstaert
 */
public interface FileResultHandler {

    /**
     * Writes the identifications to file
     *
     * @param resultFile the spectrum annotation pipeline result
     * @param identifications the experiment identifications
     */
    void writeResult(File resultFile, List<Identification> identifications);

    /**
     * Parses the result file and returns the SpectrumAnnotatorResult
     *
     * @param resultFile the spectrum annotation pipeline result
     * @return the spectrum annotator result
     */
    SpectrumAnnotatorResult readResult(File resultFile);
}
