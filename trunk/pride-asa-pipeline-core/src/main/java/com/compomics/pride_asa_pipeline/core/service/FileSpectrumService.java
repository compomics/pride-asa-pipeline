/*
 *

 */
package com.compomics.pride_asa_pipeline.core.service;

import com.compomics.respindataextractor.dataextraction.extractors.parameters.FileParser;

/**
 *
 * @author Niels Hulstaert
 */
public interface FileSpectrumService extends SpectrumService {

    /**
     * Set the FileParser
     *
     * @param fileParser
     */
    void setFileParser(FileParser fileParser);

}
