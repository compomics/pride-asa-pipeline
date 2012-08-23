/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pride_asa_pipeline.repository;

import com.compomics.pride_asa_pipeline.model.Identification;
import java.io.File;
import java.util.List;

/**
 *
 * @author niels
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
     * Reads the result file
     * 
     * @param resultFile the spectrum annotation pipeline result     
     */
    void readResult(File resultFile);
}
