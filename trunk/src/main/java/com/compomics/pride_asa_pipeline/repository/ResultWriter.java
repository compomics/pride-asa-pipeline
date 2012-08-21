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
public interface ResultWriter {
        
    /**
     * Writes the identifications to file
     * 
     * @param resultFile
     * @param identifications
     */
    void writeResult(File resultFile, List<Identification> identifications);
    
}
