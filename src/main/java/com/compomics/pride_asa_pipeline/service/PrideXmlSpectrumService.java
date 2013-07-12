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
    
    void setPrideXmlParser(PrideXmlParser prideXmlParser);

}
