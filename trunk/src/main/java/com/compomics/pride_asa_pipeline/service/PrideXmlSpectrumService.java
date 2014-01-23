/*
 *

 */
package com.compomics.pride_asa_pipeline.service;

import com.compomics.pride_asa_pipeline.repository.PrideXmlParser;

/**
 *
 * @author Niels Hulstaert
 */
public interface PrideXmlSpectrumService extends SpectrumService {
    
     /**
     * Set the PrideXmlParser
     * 
     * @param prideXmlParser 
     */
    void setPrideXmlParser(PrideXmlParser prideXmlParser);

}
